pipeline {
  agent any

  environment {
    DOCKER_IMAGE = "kuraerut/spring-todo-app"
    DOCKER_TAG = "latest"
  }

  stages {
    stage('Test PR') {
      when {
        branch 'dev'
        changeRequest()
      }
      steps {
        sh 'mvn clean install'
      }
    }

    stage('Build & Push Docker Image') {
      when {
        branch 'main'
      }
      steps {
        script {
          sh 'mvn clean package'

          withCredentials([usernamePassword(
            credentialsId: 'docker-hub-creds',
            usernameVariable: 'DOCKER_USER',
            passwordVariable: 'DOCKER_PASS'
          )]) {
            sh "echo ${DOCKER_PASS} | docker login -u ${DOCKER_USER} --password-stdin"

            docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")
            docker.push("${DOCKER_IMAGE}:${DOCKER_TAG}")
          }
        }
      }
    }

    stage('Deploy to K8s') {
      when {
        branch 'main'
      }
      steps {
        sh """
          kubectl apply -f k8s/postgres-secret.yaml
          kubectl apply -f k8s/postgres-deployment.yaml
          kubectl apply -f k8s/redis-deployment.yaml
          kubectl rollout restart deployment/spring-app
        """
      }
    }
  }
}