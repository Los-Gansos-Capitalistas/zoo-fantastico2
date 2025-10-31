#!/usr/bin/env bash
set -euo pipefail

echo "▶ Verificando Docker Desktop…"
if ! pgrep -f "/Applications/Docker.app/Contents/MacOS/Docker" >/dev/null 2>&1; then
  echo "• Lanzando Docker Desktop"
  open -a Docker
fi

echo "• Esperando daemon…"
i=0; until docker info >/dev/null 2>&1; do
  ((i++)); if (( i>90 )); then
    echo "✖ Docker no inició. Intenta: pkill -f Docker && open -a Docker"
    exit 1
  fi
  sleep 2
done
echo "✓ Docker listo"

echo "▶ docker compose up"
docker compose down -v --remove-orphans || true
docker compose up -d
docker compose ps
echo "▶ Logs (Ctrl+C para salir)"
docker logs -f zoo-app