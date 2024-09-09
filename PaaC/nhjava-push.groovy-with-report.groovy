pipeline {
    agent { label 'superlinux' } // Limit to node named superlinux

    environment {
        SONARQUBE_URL = 'http://192.168.1.85:9000'
        SONARQUBE_TOKEN = credentials('XP_2600') // Use Jenkins credentials for security
        GIT_REPO = 'https://github.com/XP2600-hub/nhorizon-java-container.git'
        DOCKER_IMAGE = 'xp2600/japp:latest'
        DOCKER_CREDENTIALS_ID = 'dockerhub_id'
        PROJECT_KEY = 'myProjectKey' // Define your project key here
    }

    stages {
        stage('Checkout Code') {
            steps {
                git url: "${GIT_REPO}", branch: 'master' // Specify the branch if needed
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    sh "docker build -t ${DOCKER_IMAGE} ."
                }
            }
        }

        stage('Login to Docker Hub') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', DOCKER_CREDENTIALS_ID) {
                        sh "docker push ${DOCKER_IMAGE}"
                    }
                }
            }
        }
 
        stage('SonarQube Scan') {
            steps {
                script {
                    sh """
                    docker run --rm \
                        -e SONAR_HOST_URL=${SONARQUBE_URL} \
                        -e SONAR_TOKEN=${SONARQUBE_TOKEN} \
                        -v \$(pwd):/usr/src \
                        -w /usr/src \
                        sonarsource/sonar-scanner-cli \
                        -Dsonar.projectKey=${PROJECT_KEY} \
                        -Dsonar.sources=. \
                        -Dsonar.docker.image=${DOCKER_IMAGE}
                    """
                }
            }
        }

        stage('SonarQube Report') {
            steps {
                script {
                    waitUntil {
                        def sonarResult = sh(script: "curl -s -u ${SONARQUBE_TOKEN}: ${SONARQUBE_URL}/api/project_analyses/search?project=${PROJECT_KEY}", returnStdout: true)
                        return sonarResult.contains('\"status\":\"SUCCESS\"')
                    }
                    echo "SonarQube analysis completed successfully."
                    def metrics = sh(script: "curl -s -u ${SONARQUBE_TOKEN}: ${SONARQUBE_URL}/api/measures/component?component=${PROJECT_KEY}&metricKeys=ncloc,coverage,bugs,vulnerabilities,code_smells", returnStdout: true)
                    echo "SonarQube Metrics: ${metrics}"
                }
            }
        }
    }
    
    post {
        success {
            echo 'Scan completed successfully.'
        }
        failure {
            echo 'Scan failed.'
        }
    }
}
