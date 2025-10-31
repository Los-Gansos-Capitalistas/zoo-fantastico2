pipeline {
  agent any

  options {
    timestamps()
    buildDiscarder(logRotator(numToKeepStr: '15'))
    ansiColor('xterm')
  }

  parameters {
    string(name: 'USERS', defaultValue: '50', description: 'Usuarios (threads)')
    string(name: 'RAMP', defaultValue: '50', description: 'Ramp-up (segundos)')
    string(name: 'LOOPS', defaultValue: '1', description: 'Ciclos por usuario')
    string(name: 'CSV', defaultValue: 'data/creatures.csv', description: 'Ruta al CSV')
    choice(name: 'ENV', choices: ['local','qa','prod'], description: 'Entorno')
    string(name: 'P95_LIMIT_MS', defaultValue: '150', description: 'Umbral p95 (ms)')
    string(name: 'ERR_LIMIT_PCT', defaultValue: '0', description: 'Umbral errores (%)')
  }
  stages {

    // -------------------------
    //  Checkout del código
    // -------------------------
    stage('Checkout') {
      steps {
        checkout scm
        sh 'ls -la'
      }
    }

    // -------------------------
    //  Levantar entorno local
    // -------------------------
    stage('Build & Start Docker') {
      steps {
        sh '''
          echo "▶ Levantando entorno local Zoo Fantástico..."
          docker compose up -d

          echo " Esperando API..."
          for i in {1..30}; do
            if curl -fsS http://host.docker.internal:8080/actuator/health | grep -q '"status":"UP"'; then
              echo " API disponible"
              break
            fi
            echo "Esperando API... ($i/30)"
            sleep 3
          done
        '''
      }
    }

    // -------------------------
    //  Tests Postman (API funcional)
    // -------------------------
    stage('Run Postman (Newman)') {
      agent {
        docker {
          image 'postman/newman:alpine'
          args '-v $PWD:/etc/newman'
        }
      }
      steps {
        sh '''
          echo "▶ Ejecutando colección Postman..."
          mkdir -p reports

          newman run postman/Zoo_Fantastico_CRUD_Creatures.postman_collection.json \
            -e postman/Zoo_Fantastico_Local.postman_environment.json \
            -r cli,htmlextra,junitfull \
            --reporter-htmlextra-export reports/postman.html \
            --reporter-junitfull-export reports/postman.junit.xml
        '''
      }
      post {
        always {
          junit 'reports/postman.junit.xml'
          archiveArtifacts artifacts: 'reports/postman.html', onlyIfSuccessful: false
        }
      }
    }

    // -------------------------
    //  Prueba de carga con JMeter
    // -------------------------
    stage('Run Load Test (JMeter)') {
      agent {
        docker {
          image 'justb4/jmeter:5.6.3'
          args '-v $PWD:/loadtest -w /loadtest'
        }
      }
      steps {
        sh '''
          echo "▶ Ejecutando prueba de carga JMeter..."
          chmod +x run-load.sh

          ./run-load.sh \
            -t /loadtest/tests/creatures-load-ci.jmx \
            -u "${USERS}" -r "${RAMP}" -l "${LOOPS}" \
            -c "${CSV}" --env="${ENV}" --p95="${P95_LIMIT_MS}" --err="${ERR_LIMIT_PCT}"
        '''
      }
    }

    // -------------------------
    //  Publicar reportes HTML
    // -------------------------
    stage('Publish Reports') {
      steps {
        script {
          echo " Publicando reportes..."

          // Detectar carpeta del último run-load
          def latest = sh(script: 'readlink -f report-latest', returnStdout: true).trim()

          publishHTML([
            allowMissing: false,
            alwaysLinkToLastBuild: true,
            keepAll: true,
            reportDir: latest,
            reportFiles: 'index.html',
            reportName: 'JMeter Dashboard'
          ])
        }
      }
    }
  }

  // -------------------------
  // Post-build notifications
  // -------------------------
  post {
    always {
      echo ' Limpieza: apagando contenedores...'
      sh 'docker compose down -v || true'
    }
    success {
      echo 'Pipeline completado correctamente.'
    }
    failure {
      echo ' Pipeline falló: revisa los reportes HTML.'
    }
  }
}
