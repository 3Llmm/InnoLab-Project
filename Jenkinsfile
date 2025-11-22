pipeline {
    agent any

    environment {
        COMPOSE_FILE = 'docker-compose.yml'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Refresh & Deploy') {
            steps {
                script {
                    echo '--- Building and Deploying on Local Host ---'
                    sh "docker compose -f ${COMPOSE_FILE} build --no-cache"
                    sh "docker compose -f ${COMPOSE_FILE} up -d --remove-orphans"
                }
            }
        }

        stage('Health Check') {
            steps {
                script {
                    sleep 10 
                    sh "docker compose -f ${COMPOSE_FILE} ps"
                }
            }
        }
    }

    post {
        success {
            echo 'Deployment updated successfully.'
            sh 'docker image prune -f' 
        }
    }
}
