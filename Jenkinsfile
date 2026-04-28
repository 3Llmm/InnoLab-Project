pipeline {
    agent any

    environment {
        COMPOSE_FILE = 'docker-compose.yml'
        BACKEND_IMAGE = 'if23b090/ctfbackend'
        FRONTEND_IMAGE = 'if23b090/ctffrontend'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Backup Current Images') {
            steps {
                script {
                    echo '--- Backing up current images for rollback ---'
                    sh "docker tag ${BACKEND_IMAGE}:latest ${BACKEND_IMAGE}:previous || true"
                    sh "docker tag ${FRONTEND_IMAGE}:latest ${FRONTEND_IMAGE}:previous || true"
                }
            }
        }

        stage('Build & Deploy') {
            steps {
                script {
                    echo '--- Building and Deploying on Local Host ---'
                    // Removed --no-cache to enable Docker layer caching
                    sh "docker compose -f ${COMPOSE_FILE} build"
                    sh "docker compose -f ${COMPOSE_FILE} up -d --remove-orphans"
                }
            }
        }

        stage('Health Check') {
            steps {
                script {
                    echo '--- Waiting for services to start ---'
                    sleep 15
                    echo '--- Checking service status ---'
                    sh "docker compose -f ${COMPOSE_FILE} ps"
                    
                    echo '--- Testing backend health ---'
                    sh "curl --fail http://localhost:8080/api/health || exit 1"
                    
                    echo '--- Testing frontend availability ---'
                    sh "curl --fail http://localhost:3000 || exit 1"
                }
            }
        }
    }

    post {
        success {
            echo 'Deployment updated successfully.'
            // Clean up unused images
            sh 'docker image prune -f'
            // Optional: Add Slack/email notification here
            // slackSend(color: 'good', message: 'CTF Platform deployed successfully!') || true
        }
        failure {
            echo 'Deployment failed! Rolling back to previous version...'
            script {
                // Rollback to previous images
                sh "docker compose -f ${COMPOSE_FILE} down || true"
                sh "docker tag ${BACKEND_IMAGE}:previous ${BACKEND_IMAGE}:latest || true"
                sh "docker tag ${FRONTEND_IMAGE}:previous ${FRONTEND_IMAGE}:latest || true"
                sh "docker compose -f ${COMPOSE_FILE} up -d --remove-orphans || true"
                echo 'Rollback complete. Please check logs for failure reason.'
            }
            // Optional: Add failure notification
            // slackSend(color: 'danger', message: 'CTF Platform deployment failed! Rolled back.') || true
        }
        always {
            // Clean up stopped containers
            sh 'docker container prune -f || true'
        }
    }
}
