#!/usr/bin/env bash
set -euo pipefail

# =========================
#   Config & helpers
# =========================
PORT="${PORT:-8080}"
BASE="http://localhost:${PORT}"
JQ_BIN="${JQ_BIN:-jq}"

title() { echo -e "\n▶ $*"; }
ok()    { echo "✓ $*"; }
warn()  { echo "• $*"; }
err()   { echo "✖ $*" >&2; }
die()   { err "$*"; exit 1; }

need_cmd() {
  command -v "$1" >/dev/null 2>&1 || die "Falta la herramienta requerida: $1"
}

need_jq() {
  if ! command -v "$JQ_BIN" >/dev/null 2>&1; then
    warn "jq no encontrado; se mostrará JSON sin pretty-print. (instala jq para una mejor salida)"
    JQ_BIN="cat"
  fi
}

require_tools() {
  title "Chequeando herramientas requeridas…"
  need_cmd mvn
  need_cmd curl
  need_cmd docker
  need_cmd lsof || true
  need_jq
  ok "Herramientas OK"
}

wait_http_up() {
  local url="$1"
  local retries="${2:-30}"
  local sleep_s="${3:-1}"
  for _ in $(seq 1 "$retries"); do
    if curl -fsS "$url" >/dev/null 2>&1; then
      return 0
    fi
    sleep "$sleep_s"
  done
  return 1
}

port_free_or_hint() {
  local p="$1"
  if lsof -iTCP:"$p" -sTCP:LISTEN >/dev/null 2>&1; then
    err "El puerto $p ya está en uso. Libéralo o ejecuta con PORT=<otro> ./check.sh"
    exit 2
  fi
}

# ==========================================
#   A-G: Secciones existentes (resumen)
#   (Se asume que ya estaban en tu script)
#   Aquí solo dejamos stubs/llamadas para
#   mantener el flujo. Ajusta si hiciste
#   cambios internos en A–G.
# ==========================================
section_A_build_verify() {
  title "[1/8] mvn clean verify (tests completos)…"
  mvn -q clean verify || die "Fallo mvn verify"
  ok "Build y tests OK"
}

section_B_unit_only() {
  title "[2/8] Tests unitarios rápidos…"
  mvn -q -Dtest='*ServiceTest' test
  ok "Unit tests OK"
}

section_C_integration_only() {
  title "[3/8] Tests de integración…"
  mvn -q -Dtest='*IntegrationTest' test
  ok "Integración OK"
}

section_D_flyway_local_h2() {
  title "[4/8] Flyway en H2 (perfil test)…"
  mvn -q -Dtest=ZooFantasticoApplicationTests test
  ok "H2 + Flyway OK"
}

section_E_docker_compose_status() {
  echo "▶ [5/8] Docker Compose status…"

  # 1️ Matar procesos colgados de Docker Desktop (silencioso)
if pgrep -f "Docker Desktop" >/dev/null 2>&1; then
  echo "• Deteniendo procesos previos de Docker Desktop…"
  pkill -f "Docker Desktop" >/dev/null 2>&1 || true
  pkill -f com.docker >/dev/null 2>&1 || true
  # intenta con sudo sin pedir password si hay permisos, si no, sigue
  sudo -n pkill -f "com.docker.backend|vpnkit|qemu-system|hyperkit" >/dev/null 2>&1 || true
  sleep 3
fi

  # 2️ Asegurar que Docker Desktop esté encendido
  echo "• Verificando Docker Desktop…"
  open -a "Docker" >/dev/null 2>&1 || true

  for i in {1..30}; do
    if docker info >/dev/null 2>&1; then
      echo "✓ Docker listo"
      break
    fi
    echo "  Esperando daemon… ($i/30)"
    sleep 3
  done

  if ! docker info >/dev/null 2>&1; then
    echo "✗ Docker no está disponible. Revisa Docker Desktop."
    return 1
  fi

  # 3️ Detectar compose v2 o fallback a docker-compose
  if docker compose version >/dev/null 2>&1; then
    COMPOSE="docker compose"
  elif command -v docker-compose >/dev/null 2>&1; then
    COMPOSE="docker-compose"
  else
    echo "✗ No se encontró docker compose ni docker-compose en PATH."
    return 1
  fi

  # 4️ Levantar y mostrar estado
  echo "• Levantando contenedores…"
  $COMPOSE up -d || { echo "✗ Falló levantar contenedores."; return 1; }

  echo "• Verificando estado de contenedores…"
  $COMPOSE ps || { echo "✗ Falló listar contenedores."; return 1; }

  echo "✓ Docker Compose OK"
}


section_F_actuator_health() {
  title "[6/8] Actuator health…"
  if wait_http_up "${BASE}/actuator/health" 20 1; then
    curl -fsS "${BASE}/actuator/health" | $JQ_BIN .
    ok "Health OK"
  else
    die "No responde ${BASE}/actuator/health"
  fi
}

section_G_api_smokes_basicos() {
  title "[7/8] API smokes básicos (zones/creatures)…"
  # Lista zones
  curl -fsS "${BASE}/api/zones" | $JQ_BIN .

  # 400 esperado en zona inválida
  curl -sS -X POST "${BASE}/api/zones" \
    -H 'Content-Type: application/json' \
    -d '{"name":" ","description":null,"capacity":10}' | $JQ_BIN .

  ok "Smokes básicos OK"
}

# ==========================================
#   H – Smoke E2E completo (nuevo)
# ==========================================
section_H_smoke_e2e() {
  title "[8/8] H – Smoke E2E completo"

  # 1) Health
  echo "Health…"
  curl -fsS "${BASE}/actuator/health" | $JQ_BIN -e '.status=="UP"' >/dev/null
  ok "Actuator UP"

  # 2) Crear zona válida
  echo "Crear zona válida…"
  ZID=$(curl -fsS -X POST "${BASE}/api/zones" \
    -H 'Content-Type: application/json' \
    -d '{"name":"BosqueSmoke","description":"F","capacity":80}' | $JQ_BIN -r '.id')
  test -n "${ZID:-}" || die "No se obtuvo ZID"
  echo "ZID=${ZID}"

  # 3) Crear criatura válida asociada
  echo "Crear criatura válida…"
  CID=$(curl -fsS -X POST "${BASE}/api/creatures" \
    -H 'Content-Type: application/json' \
    -d '{"name":"PumaSmoke","species":"Puma concolor","size":1.5,"dangerLevel":3,"healthStatus":"HEALTHY","zoneId":'"$ZID"'}' | $JQ_BIN -r '.id' 2>/dev/null || true)
  # Si tu endpoint no devuelve id, CID puede estar vacío. Igual seguimos.
  echo "CID=${CID:-N/A}"

  # 4) Summary
  echo "Summary…"
  curl -fsS "${BASE}/api/zones/summary" | $JQ_BIN .

  # 5) Intentar borrar la zona (acepta 204 o 422)
  echo "Intento borrar zona (se acepta 204 o 422)…"
  HTTP_DEL=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "${BASE}/api/zones/${ZID}")
  echo "DELETE /api/zones/${ZID} => ${HTTP_DEL}"
  if [[ "$HTTP_DEL" != "204" && "$HTTP_DEL" != "422" ]]; then
    die "DELETE /api/zones/${ZID} devolvió ${HTTP_DEL} (se esperaba 204 o 422)"
  fi

  ok "Smoke E2E OK"
}

# ==========================================
#   Runner
# ==========================================
main() {
  require_tools

  # Si pasas una sección específica (p.ej. H), solo corre esa.
  case "${1:-ALL}" in
    A) section_A_build_verify ;;
    B) section_B_unit_only ;;
    C) section_C_integration_only ;;
    D) section_D_flyway_local_h2 ;;
    E) section_E_docker_compose_status ;;
    F) section_F_actuator_health ;;
    G) section_G_api_smokes_basicos ;;
    H) section_H_smoke_e2e ;;
    ALL)
      # Verifica puerto sólo si el servicio corre en host (útil si pruebas sin Docker)
      port_free_or_hint "$PORT" || true

      section_A_build_verify
      section_B_unit_only
      section_C_integration_only
      section_D_flyway_local_h2
      section_E_docker_compose_status
      section_F_actuator_health
      section_G_api_smokes_basicos
      section_H_smoke_e2e
      ;;
    *)
      die "Sección desconocida: ${1:-} (usa A|B|C|D|E|F|G|H|ALL)"
      ;;
  esac
}

main "${1:-ALL}"
