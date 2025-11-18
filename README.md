ZOO FANTÁSTICO – PROYECTO CRUD COMPLETO CON CI/CD
Principios de Desarrollo de Software
Este proyecto implementa un CRUD completo para gestionar criaturas y zonas del “Zoo Fantástico”, incluyendo:
API REST con Spring Boot
Validaciones con DTOs
Mapeos y servicio de dominio
Pruebas unitarias y de integración
Pruebas funcionales (Postman)
Pruebas de carga (JMeter)
Empaquetado en JAR
Despliegue con Docker
Orquestación con Docker Compose
Pipeline CI/CD completo con Jenkins
ESTRUCTURA DEL PROYECTO
zoo-fantastico2/
├── src/
│ ├── main/java/com/zoo/zoofantastico/
│ │ ├── controller/
│ │ ├── service/
│ │ ├── repository/
│ │ ├── model/
│ │ ├── dto/
│ │ ├── exception/
│ │ └── ZooFantasticoApplication.java
│ └── resources/
│ ├── application.yml
│ └── db/migration/V1__init_schema.sql
├── tests/ (JMeter)
├── postman/ (Colecciones)
├── docker-compose.yml
├── Dockerfile
├── Jenkinsfile
├── mvnw (wrapper)
└── pom.xml
CRUD EN SPRING BOOT
Endpoints principales:
CRIATURAS
GET /api/creatures
POST /api/creatures
PUT /api/creatures/{id}
DELETE /api/creatures/{id}
ZONAS
GET /api/zones
POST /api/zones
Incluye:
Validación con Bean Validation
Excepciones centralizadas
Mapeo DTO
Repositorios Spring Data
Capa de servicio con reglas de negocio
PRUEBAS UNITARIAS E INTEGRACIÓN
Pruebas creadas:
Controladores con MockMvc
Servicios con Mockito
Integración con H2 y SpringBootTest
Resultado final:
79 pruebas exitosas, 0 fallos.
PRUEBAS FUNCIONALES (POSTMAN)
Incluye:
Colección: Zoo_Fantastico_CRUD_Creatures.postman_collection.json
Entorno: Zoo_Fantastico_Local.postman_environment.json
Validaciones en scripts de Pre y Post Request
Reporte HTML generado con Newman
PRUEBAS DE CARGA CON JMETER
Plan de prueba JMX con:
Thread group parametrizable
CSV DataSet configurado
Validaciones
Dashboard HTML
Script ejecutable:
./run-load.sh -t tests/creatures-load-ci.jmx -u 50 -r 50 -l 1 -c data/creatures.csv
EMPAQUETADO EN JAR
Comandos:
mvn clean package
java -jar target/zoo-fantastico-0.0.1-SNAPSHOT.jar
La aplicación inicia con base de datos H2.
DOCKER
Dockerfile:
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/zoo-fantastico-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
Docker Compose:
services:
zoo-mysql:
image: mysql:8.0
ports:
- "3306:3306"
environment:
MYSQL_DATABASE: zoo
MYSQL_ROOT_PASSWORD: root
zoo-app:
build: .
ports:
- "8082:8080"
depends_on:
- zoo-mysql
Comando:
docker compose up -d --build
CI/CD CON JENKINS
Pipeline implementado:
Build con Maven
Ejecución de pruebas
Construcción de imagen Docker
Levantamiento del contenedor
Healthcheck automático
Generación y archivado de logs
JENKINSFILE COMPLETO:
pipeline {
agent any
stages {

    stage('Build') {
        steps {
            echo "== Build: empaquetar aplicación =="
            sh 'chmod +x mvnw || true'
            sh './mvnw clean package -DskipTests=false'
        }
    }

    stage('Run Tests') {
        steps {
            echo "== Run Tests: ejecutar pruebas unitarias e integración =="
            sh './mvnw test'
        }
    }

    stage('Docker Build') {
        steps {
            echo "== Docker Build: construir imagen zoo-fantastico-app =="
            sh 'docker build -t zoo-fantastico-app:latest .'
        }
    }

    stage('Docker Run') {
        steps {
            echo "== Docker Run: levantar contenedor =="
            sh '''
              echo "▶ Limpiando contenedor previo (si existe)..."
              docker ps -q --filter name=zoo-fantastico-pipeline | xargs -r docker stop
              docker ps -aq --filter name=zoo-fantastico-pipeline | xargs -r docker rm

              echo "▶ Levantando nuevo contenedor..."
              docker run -d --name zoo-fantastico-pipeline -p 8083:8080 zoo-fantastico-app:latest

              echo "▶ Esperando que la API esté UP..."
              for i in {1..30}; do
                if curl -fsS http://localhost:8083/actuator/health | grep -q '"status":"UP"'; then
                  echo "API READY ✔"
                  break
                fi
                echo "API no está lista, esperando... ($i/30)"
                sleep 2
              done
            '''
        }
    }
}

post {
    always {
        echo 'Guardando logs del contenedor...'
        sh 'docker logs zoo-fantastico-pipeline > container.log || true'
        archiveArtifacts artifacts: 'container.log', onlyIfSuccessful: false
    }

    success {
        echo '¡Pipeline completado correctamente!'
    }

    failure {
        echo 'Pipeline falló: revisa los reportes.'
    }
}
}
AUTORES
Equipo Los Gansos Capitalistas
