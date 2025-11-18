ZOO FANTÁSTICO
Proyecto CRUD + Pruebas + Docker + CI/CD con Jenkins
====================================================================
DESCRIPCIÓN GENERAL DEL PROYECTO
====================================================================
Zoo Fantástico es un sistema CRUD completo para administrar criaturas y zonas dentro de un zoológico fantástico.
Incluye:
• API REST desarrollada con Spring Boot
• Base de datos MySQL y H2 para pruebas
• Validaciones completas con DTOs
• Pruebas unitarias e integración
• Pruebas funcionales (Postman + Newman)
• Pruebas de carga con JMeter
• Empaquetado JAR listo para producción
• Despliegue con Docker y Docker Compose
• Pipeline CI/CD completamente automatizado con Jenkins
El objetivo es demostrar el ciclo de vida completo de desarrollo, pruebas y despliegue automatizado.
====================================================================
2. REQUISITOS PREVIOS
Para ejecutar el proyecto necesitas:
• Java 17 o superior
• Maven 3.9+
• Docker y Docker Compose
• Git
• Jenkins (para la parte CI/CD)
• Postman (para pruebas manuales)
• JMeter 5.6.3 (si deseas ejecutar pruebas de carga fuera del pipeline)
====================================================================
3. CÓMO CLONAR EL PROYECTO
Abrir una terminal
Ejecutar:
git clone https://github.com/Los-Gansos-Capitalistas/zoo-fantastico2.git
cd zoo-fantastico2
La rama principal de trabajo es:
git checkout develop
====================================================================
4. CÓMO EJECUTAR EL PROYECTO MANUALMENTE
Paso 1 – Empaquetar el JAR
Ejecuta:
mvn clean package
Esto genera:
target/zoo-fantastico-0.0.1-SNAPSHOT.jar
Paso 2 – Ejecutar la aplicación
java -jar target/zoo-fantastico-0.0.1-SNAPSHOT.jar
La API levanta en:
http://localhost:8080
====================================================================
5. ENDPOINTS DISPONIBLES
CRIATURAS
GET /api/creatures
POST /api/creatures
PUT /api/creatures/{id}
DELETE /api/creatures/{id}
ZONAS
GET /api/zones
POST /api/zones
Base de datos por defecto en local:
H2 (en memoria)
====================================================================
6. EJECUTAR EL PROYECTO CON DOCKER
El proyecto incluye Dockerfile y docker-compose.
Paso 1 – Construir imagen
docker build -t zoo-fantastico-app .
Paso 2 – Ejecutar contenedores con MySQL
docker compose up -d --build
Esto crea:
• Contenedor MySQL
• Contenedor de la API Zoo Fantástico
La API queda en:
http://localhost:8082/api/creatures
Para detener:
docker compose down -v
====================================================================
7. PRUEBAS UNITARIAS E INTEGRACIÓN
El proyecto incluye 79 pruebas totales entre:
• Controladores
• Servicios
• Integración con H2
• Validaciones
Ejecutar pruebas:
mvn test
Resultado final:
TODAS las pruebas pasan correctamente.
====================================================================
8. PRUEBAS FUNCIONALES (POSTMAN)
El proyecto incluye:
• Colección Postman
• Entorno Postman
• Script run-postman.sh
• Reporte HTML generado por Newman
Ejecutar Newman manualmente:
newman run postman/Zoo_Fantastico_CRUD_Creatures.postman_collection.json
-e postman/Zoo_Fantastico_Local.postman_environment.json
-r cli,htmlextra
El reporte se guarda en /reports.
====================================================================
9. PRUEBAS DE CARGA (JMETER)
Incluye plan de pruebas creatures-load-ci.jmx
El script automatizado es:
./run-load.sh -t tests/creatures-load-ci.jmx -u 50 -r 50 -l 1 -c data/creatures.csv
Esto genera un dashboard HTML automatizado.
====================================================================
10. PIPELINE CI/CD CON JENKINS
El proyecto incluye un Jenkinsfile profesional que realiza:
Checkout automático
Compilación Maven
Ejecución de pruebas unitarias
Construcción de imagen Docker
Despliegue automático de contenedor
Healthcheck esperando que la API esté UP
Guardado de logs del contenedor como artefacto
Mensaje de éxito o error
El pipeline se ejecuta automáticamente cada vez que se hace push a la rama develop.
====================================================================
11. CONFIGURAR JENKINS
Paso 1 – Crear pipeline nuevo
Pipeline → Enter name → Pipeline (from SCM)
Paso 2 – Pegar la URL del repositorio
https://github.com/Los-Gansos-Capitalistas/zoo-fantastico2.git
Paso 3 – Seleccionar branch develop
Paso 4 – Guardar y ejecutar
El pipeline hace TODO automáticamente.
====================================================================
12. ESTRUCTURA DEL REPOSITORIO
zoo-fantastico2
├── src/main/java
├── src/test/java
├── postman
├── jmeter
├── docker-compose.yml
├── Dockerfile
├── Jenkinsfile
└── README
Organización profesional y clara.
====================================================================
13. CÓMO SE VE EL DESPLIEGUE FINAL
Al ejecutar Jenkins:
• Construye el JAR
• Ejecuta pruebas
• Crea imagen Docker
• Elimina contenedor anterior
• Levanta contenedor nuevo zoo-fantastico-pipeline
• Espera el healthcheck
• Publica logs
• Marca build como SUCCESS
Luego puedes abrir:
http://localhost:8083/api/creatures
====================================================================
14. AUTORES
Proyecto realizado por:
Los Gansos Capitalistas
====================================================================
15. LICENCIA
Uso académico permitido – MIT License.
====================================================================
FIN DEL README
