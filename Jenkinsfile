pipeline {
    agent any

    triggers {
        pollSCM('*/5 * * * *')
    }

    environment {
        DEPLOY_DIR = "/opt/ctf"
        BACKEND_DIR = "${DEPLOY_DIR}/backend"
        FRONTEND_DIR = "${DEPLOY_DIR}/frontend"
        TERMINAL_DIR = "${DEPLOY_DIR}/terminal"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Backend') {
            steps {
                dir('ctf-backend') {
                    sh './mvnw clean package -DskipTests -Dcheckstyle.skip=true -q'
                }
            }
        }

        stage('Build Frontend') {
            steps {
                dir('ctf-frontend') {
                    sh 'npm ci'
                    sh 'NEXT_PUBLIC_API_URL=http://inno1-bif3-p1-w25.cs.technikum-wien.at:8080 NEXT_PUBLIC_TERMINAL_URL=ws://inno1-bif3-p1-w25.cs.technikum-wien.at:80/terminal npm run build'
                }
            }
        }

        stage('Deploy Backend') {
            steps {
                script {
                    echo '--- Deploying Backend ---'
                    sh '''
                        BACKEND_JAR=""
                        if [ -f ctf-backend/target/app.jar ]; then
                            BACKEND_JAR=ctf-backend/target/app.jar
                        elif [ -n "$(find ctf-backend/target -maxdepth 1 -type f -name "*.jar" 2>/dev/null | head -n1)" ]; then
                            BACKEND_JAR=$(find ctf-backend/target -maxdepth 1 -type f -name "*.jar" 2>/dev/null | head -n1)
                        elif [ -n "$(find ctf-backend/build/libs -maxdepth 1 -type f -name "*.jar" 2>/dev/null | head -n1)" ]; then
                            BACKEND_JAR=$(find ctf-backend/build/libs -maxdepth 1 -type f -name "*.jar" 2>/dev/null | head -n1)
                        fi

                        if [ -z "$BACKEND_JAR" ]; then
                            echo "ERROR: No backend JAR found"
                            exit 1
                        fi

                        echo "Deploying $BACKEND_JAR to ${BACKEND_DIR}/app.jar"
                        cp "$BACKEND_JAR" "${BACKEND_DIR}/app.jar"
                    '''
                }
            }
        }

        stage('Deploy Frontend') {
            steps {
                script {
                    echo '--- Deploying Frontend ---'
                    sh """
                        rm -rf ${FRONTEND_DIR}/.next
                        cp -r ctf-frontend/.next ${FRONTEND_DIR}/
                        ln -sf ${WORKSPACE}/ctf-frontend/package.json ${FRONTEND_DIR}/package.json
                        ln -sf ${WORKSPACE}/ctf-frontend/package-lock.json ${FRONTEND_DIR}/package-lock.json
                    """
                }
            }
        }

        stage('Deploy Terminal') {
            steps {
                script {
                    echo '--- Deploying Terminal ---'
                    sh "cp ctf-terminal/server.js ${TERMINAL_DIR}/server.js"
                }
            }
        }

        stage('Restart Services') {
            steps {
                script {
                    echo '--- Restarting Services ---'
                    sh 'systemctl restart ctf-backend ctf-frontend ctf-terminal'
                    sleep 3
                }
            }
        }

        stage('Health Check') {
            steps {
                script {
                    echo '--- Running Health Checks ---'

                    sh 'curl -sf http://localhost:3000 > /dev/null || (echo "Frontend health check failed" && exit 1)'
                    echo 'Frontend: OK'

                    sh 'curl -sf http://localhost:3001/health > /dev/null || (echo "Terminal health check failed" && exit 1)'
                    echo 'Terminal: OK'

                    timeout(time: 30, unit: 'SECONDS') {
                        waitUntil {
                            try {
                                sh 'curl -sf http://localhost:8080/api/health > /dev/null'
                                return true
                            } catch (Exception e) {
                                sleep 1
                                return false
                            }
                        }
                    }
                    echo 'Backend: OK'
                }
            }
        }
    }

    post {
        success {
            echo 'Deployment completed successfully!'
            echo 'Hard refresh browser (Ctrl+Shift+R) to see changes'
        }
        failure {
            echo 'Deployment failed! Check the logs above for details.'
        }
    }
}