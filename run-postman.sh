#!/usr/bin/env bash
# run-postman.sh - Ejecuta la colección con Newman y genera reporte HTML y JSON.
# Requisitos: Node.js + newman + newman-reporter-htmlextra
#   npm install -g newman newman-reporter-htmlextra
#
# Uso:
#   ./run-postman.sh "Zoo Fantástico - CRUD Creatures.postman_collection.json" zoo-fantastico-local.postman_environment.json
#
set -euo pipefail

COLLECTION_JSON="${1:-postman/Zoo_Fantastico_CRUD_Creatures.postman_collection.json}"
ENV_JSON="${2:-postman/Zoo_Fantastico_Local.postman_environment.json}"
OUT_DIR="${3:-postman-report-$(date +%Y%m%d-%H%M%S)}"

mkdir -p "$OUT_DIR"

# Ejecuta con reporte HTML + JSON
newman run "$COLLECTION_JSON" --environment "$ENV_JSON" \
  --reporters htmlextra,json \
  --reporter-htmlextra-export "$OUT_DIR/report.html" \
  --reporter-json-export "$OUT_DIR/report.json" \
  --timeout-request 60000 \
  --delay-request 1           

echo
echo "=== Terminado ==="
echo "Reporte HTML: $OUT_DIR/report.html"
echo "Reporte JSON: $OUT_DIR/report.json"
