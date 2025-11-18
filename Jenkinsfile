pipeline {
  agent any

  options {
    timestamps()
    buildDiscarder(logRotator(numToKeepStr: '15'))
  }

  stages {

    stage('Checkout') {
      steps {
        checkout scm
        sh 'ls -la'
      }
    }

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
        echo "== Docker Build: construir imagen zoo-fantastico-app:latest =="
        sh 'docker build -t zoo-fantastico-app:latest .'
      }
    }

    stage('Docker Run') {
      steps {
        echo "== Docker Run: levantar contenedor en puerto 8083 (host) -> 8080 (contenedor) =="

        sh '''
          echo "▶ Limpiando contenedor previo (si existe)..."
          docker ps -q --filter "name=zoo-fantastico-pipeline" | xargs -r docker stop
          docker ps -aq --filter "name=zoo-fantastico-pipeline" | xargs -r docker rm

          echo "▶ Levantando nuevo contenedor..."
          docker run -d --name zoo-fantastico-pipeline -p 8083:8080 zoo-fantastico-app:latest
        '''
      }
    }
  }

  post {
    always {
      echo ' Limpieza final opcional:'
      // Si quieres que el contenedor NO quede corriendo al final, descomenta estas:
      // sh 'docker ps -q --filter "name=zoo-fantastico-pipeline" | xargs -r docker stop || true'
      // sh 'docker ps -aq --filter "name=zoo-fantastico-pipeline" | xargs -r docker rm || true'
    }
    success {
      echo 'Pipeline completado correctamente (Build + Tests + Docker Build + Docker Run).'
    }
    failure {
      echo ' Pipeline falló: revisa la consola para ver en qué etapa se cayó.'
    }
  }
}
