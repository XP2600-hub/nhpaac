pipeline {
    agent any

    environment {
        SONARQUBE_URL = 'http://192.168.1.85:9000'
        SONARQUBE_TOKEN = credentials('XP_2600') // Use Jenkins credentials for security
        DOCKER_IMAGE = 'nginx:latest'
    }

    stages {
        stage('Pull Docker Image') {
            steps {
                script {
                    // Pull the Docker image from Docker Hub
                    sh "docker pull ${DOCKER_IMAGE}"
                }
            }
        }

        stage('SonarQube Scan') {
            steps {
                script {
                    // Run SonarQube scanner inside the Docker container
                    sh """
                    docker run --rm \
                        -e SONAR_HOST_URL=${SONARQUBE_URL} \
                        -e SONAR_TOKEN=${SONARQUBE_TOKEN} \
                        -v \$(pwd):/usr/src \
                        -w /usr/src \
                        sonarsource/sonar-scanner-cli \
                        -Dsonar.projectKey=myProjectKey \
                        -Dsonar.sources=. \
                        -Dsonar.docker.image=${DOCKER_IMAGE}
                    """
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