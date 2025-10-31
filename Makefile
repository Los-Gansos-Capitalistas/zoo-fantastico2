# ==============================
# Zoo Fantástico - Makefile
# ==============================

# --- Variables comunes ---
COLLECTION   := postman/Zoo_Fantastico_CRUD_Creatures.postman_collection.json
ENVIRONMENT  := postman/Zoo_Fantastico_Local.postman_environment.json
REPORT_DIR   := postman/reports
TIMESTAMP    := $(shell date +"%Y%m%d-%H%M%S")
HTML_REPORT  := $(REPORT_DIR)/postman-report-$(TIMESTAMP).html
JSON_REPORT  := $(REPORT_DIR)/postman-report-$(TIMESTAMP).json

# Puedes sobreescribir BASE_URL al ejecutar: make postman BASE_URL=http://localhost:8081
# Si no defines BASE_URL, se usará el del environment file.
BASE_URL     ?=

# --- Dev / Infra ---
.PHONY: test run-dev stop-dev clean-dev down-clean logs wait-health

#  Ejecutar todo el flujo de pruebas
test:
	@echo " Ejecutando flujo completo de validación (unit + integration)..."
	./run_full_test.sh

#  Levantar entorno de desarrollo (MySQL + App)
run-dev:
	- docker rm -f zoo-app 2>/dev/null || true
	docker compose up --build -d --remove-orphans

#  Detener entorno de desarrollo
stop-dev:
	@echo " Deteniendo entorno de desarrollo..."
	docker compose down

#  Limpiar entorno (borra contenedores y volúmenes)
clean-dev:
	@echo " Limpiando entorno de desarrollo (contenedores + datos persistentes)..."
	docker compose down -v

down-clean:
	docker compose down -v --remove-orphans

logs:
	docker compose logs -f --tail=200

# Espera a que la app esté ready (actuator)
wait-health:
	@echo "Esperando API en http://localhost:8080/actuator/health ..."
	@for i in $$(seq 1 30); do \
		if curl -fsS http://localhost:8080/actuator/health | grep -q '"status":"UP"'; then \
			echo "API UP "; exit 0; \
		fi; \
		echo "Esperando API... ($$i/30)"; sleep 2; \
	done; \
	echo " La API no levantó a tiempo"; exit 1

# --- Postman (host) ---
.PHONY: postman clean-reports

postman:
	@echo "▶ Ejecutando pruebas Postman con Newman (host)..."
	@mkdir -p "$(REPORT_DIR)"
	@if [ -n "$(BASE_URL)" ]; then \
		echo "Override baseUrl = $(BASE_URL)"; \
		newman run "$(COLLECTION)" \
			-e "$(ENVIRONMENT)" \
			--env-var baseUrl="$(BASE_URL)" \
			-r cli,htmlextra,json \
			--reporter-htmlextra-export "$(HTML_REPORT)" \
			--reporter-json-export "$(JSON_REPORT)" \
			--timeout-request 60000; \
	else \
		newman run "$(COLLECTION)" \
			-e "$(ENVIRONMENT)" \
			-r cli,htmlextra,json \
			--reporter-htmlextra-export "$(HTML_REPORT)" \
			--reporter-json-export "$(JSON_REPORT)" \
			--timeout-request 60000; \
	fi
	@echo
	@echo "===  Pruebas completadas ==="
	@echo "Reporte HTML: $(HTML_REPORT)"
	@echo "Reporte JSON: $(JSON_REPORT)"
	@echo
	@echo "Abriendo reporte..."
	@open "$(HTML_REPORT)" 2>/dev/null || echo "Abre manualmente: $(HTML_REPORT)"

# Limpieza rápida de reports
clean-reports:
	@echo " Limpiando reportes antiguos..."
	@rm -f $(REPORT_DIR)/*.html $(REPORT_DIR)/*.json || true
	@echo " Limpieza completada."

# --- Postman dentro de Docker ---
# En macOS/Windows Docker Desktop, host.docker.internal funciona.
# En Linux, añadimos --add-host=host.docker.internal:host-gateway para resolver el host.
.PHONY: postman-docker

postman-docker: run-dev wait-health
	@echo "Ejecutando colección Postman con imagen postman/newman..."
	@mkdir -p "$(REPORT_DIR)"
	@docker run --rm \
	  -v "$(PWD)/postman:/etc/newman" \
	  -v "$(PWD)/$(REPORT_DIR):/etc/newman/reports" \
	  --add-host=host.docker.internal:host-gateway \
	  --entrypoint sh \
	  postman/newman:alpine -c '\
	    npm i -g newman-reporter-htmlextra newman-reporter-junitfull >/dev/null 2>&1 && \
	    newman run /etc/newman/Zoo_Fantastico_CRUD_Creatures.postman_collection.json \
	      -e /etc/newman/Zoo_Fantastico_Local.postman_environment.json \
	      $$( [ -n "$(BASE_URL)" ] && echo --env-var baseUrl=$(BASE_URL) || echo --env-var baseUrl=http://host.docker.internal:8080 ) \
	      -r cli,htmlextra,junitfull \
	      --reporter-htmlextra-export "/etc/newman/reports/postman-$(TIMESTAMP).html" \
	      --reporter-junitfull-export "/etc/newman/reports/postman-$(TIMESTAMP).junit.xml" \
	  '
	@echo " Reportes en $(REPORT_DIR)/postman-$(TIMESTAMP).html y .junit.xml"
