pipeline {
  agent any

  triggers {
          githubPush()

  }

  stages {
    stage('Checkout') {
      steps {
        checkout([
          $class: 'GitSCM',
          branches: [[name: env.CHANGE_BRANCH ?: env.BRANCH_NAME]],
          extensions: [
            [$class: 'CloneOption', depth: 1],
            [$class: 'LocalBranch']
          ],
          userRemoteConfigs: [[url: env.GIT_URL]]
        ])
      }
    }

    stage('Build & Test') {
      steps {
        sh 'mvn clean install'
      }
    }
  }
}