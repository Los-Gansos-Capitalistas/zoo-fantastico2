#!/usr/bin/env bash
set -euo pipefail

# =========================
# Defaults (overridables)
# =========================
TESTPLAN="${TESTPLAN:-jmeter/creatures-load-4.jmx}"
USERS="${USERS:-50}"
RAMP="${RAMP:-50}"
LOOPS="${LOOPS:-1}"
CSV="${CSV:-jmeter/data/creatures-2.csv}"
OUT_BASE="${OUT_BASE:-./report}"

# Quality gates 
ENV="${ENV:-local}"
P95_LIMIT_MS="${P95_LIMIT_MS:-150}"
ERR_LIMIT_PCT="${ERR_LIMIT_PCT:-0}"

# =========================
# Parseo flags cortos
# =========================
usage() {
  cat <<'EOF'
Uso: run-load.sh [-t testplan] [-u users] [-r ramp] [-l loops] [-c csv] [--env=local|qa|prod] [--p95=MS] [--err=PCT] [-- JMETER_EXTRA_ARGS...]
Ejemplo:
  run-load.sh -t tests/creatures-load-ci.jmx -u 50 -r 50 -l 1 -c data/creatures.csv --env=local --p95=150 --err=0
EOF
}
while getopts ":t:u:r:l:c:h" opt; do
  case ${opt} in
    t) TESTPLAN="$OPTARG" ;;
    u) USERS="$OPTARG" ;;
    r) RAMP="$OPTARG" ;;
    l) LOOPS="$OPTARG" ;;
    c) CSV="$OPTARG" ;;
    h) usage; exit 0 ;;
    \?) echo "Flag inválido: -$OPTARG" >&2; usage; exit 2 ;;
    :)  echo "Flag -$OPTARG requiere valor" >&2; usage; exit 2 ;;
  esac
done
shift $((OPTIND-1))

# =========================
# Flags largos + extras tras "--"
# =========================
JMETER_EXTRA=()   # ← importante para evitar 'unbound variable'
for arg in "$@"; do
  case "$arg" in
    --env=*) ENV="${arg#*=}";;
    --p95=*) P95_LIMIT_MS="${arg#*=}";;
    --err=*) ERR_LIMIT_PCT="${arg#*=}";;
    --)      shift; JMETER_EXTRA=("$@"); break;;
    --*)     echo "Flag largo desconocido: $arg" >&2; exit 2;;
    *)       ;;  # ignorar restos
  esac
done

# =========================
# Detección de entorno → host/port
# =========================
detect_host() {
  if [[ -n "${JM_HOST:-}" ]]; then echo "$JM_HOST"; return; fi
  if [[ -n "${JENKINS_URL:-}" || -f "/.dockerenv" ]]; then
    if getent hosts host.docker.internal >/dev/null 2>&1 || nslookup host.docker.internal >/dev/null 2>&1; then
      echo "host.docker.internal"; return
    fi
    gw="$(/sbin/ip route 2>/dev/null | awk '/default/ {print $3; exit}')"
    if [[ -n "$gw" ]]; then echo "$gw"; return; fi
  fi
  # Forzar IPv4 explícito para evitar 'Connection reset by peer' por IPv6 (::1)
  echo "127.0.0.1"
}

detect_port() {
  if [[ -n "${JM_PORT:-}" ]]; then echo "$JM_PORT"; return; fi
  case "$ENV" in
    qa|prod) echo "80" ;;
    *)       echo "8080" ;;
  esac
}

JM_HOST="$(detect_host)"
JM_PORT="$(detect_port)"

# =========================
# Mapping por entorno
# =========================
case "$ENV" in
  local) JM_TOKEN="${JM_TOKEN:-}";;
  qa)    JM_TOKEN="${JM_TOKEN:-}";;
  prod)  JM_TOKEN="${JM_TOKEN:-}";;
  *) echo "Entorno no reconocido: $ENV (usa local|qa|prod)"; exit 2;;
esac

# =========================
# Pre-checks
# =========================
command -v jmeter >/dev/null 2>&1 || { echo " No se encontró 'jmeter' en PATH."; exit 1; }
[[ -f "$TESTPLAN" ]] || { echo " Test plan no existe: $TESTPLAN"; exit 1; }
[[ -f "$CSV" ]] || { echo " CSV no existe: $CSV"; exit 1; }

# =========================
# Paths de salida
# =========================
TS="$(date +%Y%m%d-%H%M%S)"
RUN_DIR="report-${TS}"
SITE_DIR="${RUN_DIR}/site"
JTL="${RUN_DIR}/results-${TS}.jtl"
LOG="${RUN_DIR}/run.log"
mkdir -p "$RUN_DIR"

echo "▶ Ejecutando JMeter..."
cat <<INFO
  Plan:    $TESTPLAN
  Users:   $USERS
  Ramp:    $RAMP
  Loops:   $LOOPS
  CSV:     $CSV
  Host:    $JM_HOST
  Port:    $JM_PORT
  Entorno: $ENV
  Salida:  $RUN_DIR
INFO

# =========================
# Run JMeter (invocación segura)
# =========================
set -x
if ((${#JMETER_EXTRA[@]})); then
  jmeter -n -t "$TESTPLAN" \
    -Jusers="$USERS" -Jramp="$RAMP" -Jloops="$LOOPS" \
    -Jcsv="$CSV" \
    -Jhost="$JM_HOST" -Jport="$JM_PORT" -Jtoken="$JM_TOKEN" \
    -l "$JTL" -e -o "$SITE_DIR" -f \
    "${JMETER_EXTRA[@]}" 2>&1 | tee "$LOG"
else
  jmeter -n -t "$TESTPLAN" \
    -Jusers="$USERS" -Jramp="$RAMP" -Jloops="$LOOPS" \
    -Jcsv="$CSV" \
    -Jhost="$JM_HOST" -Jport="$JM_PORT" -Jtoken="$JM_TOKEN" \
    -l "$JTL" -e -o "$SITE_DIR" -f \
    2>&1 | tee "$LOG"
fi
set +x

ln -sfn "$SITE_DIR" report-latest

echo
echo " Terminado."
echo " JTL:          $JTL"
echo "  Reporte:     $SITE_DIR/index.html"
echo " Acceso rápido: ./report-latest (symlink)"
command -v open >/dev/null 2>&1 && echo " Abrir en macOS: open \"$SITE_DIR/index.html\""

# =========================
# Quality Gates (error% y p95)
# =========================
total=$(awk -F, 'NR>1{c++} END{print c+0}' "$JTL")
errs=$(awk -F, 'NR>1 && $8=="false"{e++} END{print e+0}' "$JTL")
err_pct=0
if [ "$total" -gt 0 ]; then
  err_pct=$(awk -v e="$errs" -v t="$total" 'BEGIN{printf "%.2f", (e*100.0)/t}')
fi
p95=$(awk -F, 'NR>1{print $2}' "$JTL" | LC_ALL=C sort -n | awk '{a[NR]=$1} END{if(NR==0){print 0; exit} idx=int(0.95*NR); if(idx<1) idx=1; print a[idx]}')

echo
echo "=== Quality Gates ==="
echo "Total: $total  Errores: $errs  Error%: $err_pct  p95(ms): $p95"
gate_fail=0

# --- Corregido: comparación correcta ---
awk_cmp=$(awk -v ep="$err_pct" -v lim="$ERR_LIMIT_PCT" 'BEGIN{print (ep>lim)?1:0}')
if [ "$awk_cmp" -eq 1 ]; then
  echo "  Error rate ${err_pct}% > límite ${ERR_LIMIT_PCT}%"
  gate_fail=1
fi

awk_cmp=$(awk -v val="$p95" -v lim="$P95_LIMIT_MS" 'BEGIN{print (val>lim)?1:0}')
if [ "$awk_cmp" -eq 1 ]; then
  echo "  p95 ${p95}ms > límite ${P95_LIMIT_MS}ms"
  gate_fail=1
fi


if [ "$gate_fail" -eq 0 ]; then
  echo " Gates OK"
else
  echo " Gates FAIL"; exit 1
fi

# =========================
# Resumen rápido
# =========================
echo
echo "=== Resumen rápido (si disponible) ==="
grep -E "summary \+|summary =" "$LOG" || echo "(No se encontró resumen en el log)"
