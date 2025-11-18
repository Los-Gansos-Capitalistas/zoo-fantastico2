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
            mail to: 'tu-correo@ejemplo.com',
                 subject: '✅ Zoo Fantástico – Pipeline SUCCESS',
                 body: "El pipeline zoo-fantastico-pipeline terminó correctamente.\n\nRevisa Jenkins si quieres ver detalles."
        }

        failure {
            echo 'Pipeline falló: revisa los reportes.'
            mail to: 'tu-correo@ejemplo.com',
                 subject: '❌ Zoo Fantástico – Pipeline FAILURE',
                 body: "El pipeline zoo-fantastico-pipeline FALLÓ.\n\nRevisa Jenkins para ver en qué etapa se rompió."
        }
    }
}
