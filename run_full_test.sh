#!/bin/bash
# FLUJO COMPLETO DE VALIDACIÓN DEL PROYECTO ZOO-FANTASTICO


# 1️ LIMPIEZA DE PROYECTO Y COMPILACIÓN INICIAL

echo " Limpiando y compilando..."
./mvnw clean compile -DskipTests


# 2️ VERIFICAR QUE NO HAYA OTRO SERVIDOR EN PUERTO 8080

echo " Liberando puerto 8080 si está ocupado..."
sudo lsof -t -i:8080 | xargs sudo kill -9 2>/dev/null || true


# 3️ EJECUTAR MIGRACIONES CON H2 (ENTORNO DE TEST)

echo " Ejecutando migraciones Flyway en base H2 (modo MySQL)..."
./mvnw flyway:migrate \
  -Dflyway.url="jdbc:h2:mem:zoofantastico_testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE" \
  -Dflyway.user=sa \
  -Dflyway.password= \
  -Dflyway.locations=classpath:db/migration,classpath:db/testdata \
  -Dflyway.driver=org.h2.Driver


# 4️ PRUEBAS UNITARIAS (rápidas, sin BD)

echo " Ejecutando pruebas UNITARIAS..."
SPRING_PROFILES_ACTIVE=test ./mvnw -Dtest=CreatureRequestValidationTest test


# 5️ PRUEBAS DE INTEGRACIÓN (usa H2 en memoria + Flyway)

echo " Ejecutando pruebas de INTEGRACIÓN..."
SPRING_PROFILES_ACTIVE=test ./mvnw -Dtest='*IntegrationTest' test


# 6️ ARRANCAR APLICACIÓN EN ENTORNO DE TEST

echo " Iniciando aplicación con perfil TEST..."
SPRING_PROFILES_ACTIVE=test ./mvnw spring-boot:run &
APP_PID=$!

# Espera unos segundos para que arranque
sleep 5


# 7️ VERIFICAR CONEXIÓN A LA CONSOLA H2

echo " Verifica la consola H2 en tu navegador:"
echo " http://localhost:8080/h2-console"
echo ""
echo " Configura los valores así:"
echo "   • JDBC URL: jdbc:h2:mem:testdb"
echo "   • User Name: sa"
echo "   • Password: (dejar en blanco)"
echo ""


# 8️ DETENER LA APLICACIÓN (si deseas)

read -p " Presiona ENTER para detener la aplicación..."
kill -9 $APP_PID

echo " Proceso completado correctamente."
