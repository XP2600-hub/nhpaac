pipeline {
    agent any

    environment {
        SONARQUBE_URL = 'http://192.168.1.85:9000'
        SONARQUBE_TOKEN = credentials('XP_2600') // Use Jenkins credentials for security
        GIT_REPO = 'https://github.com/XP2600-hub/nhorizon-java-container.git'
        DOCKER_IMAGE = 'xp2600/japp:latest'
        DOCKER_CREDENTIALS_ID = 'dockerhub_id'
    }

    stages {
        stage('Checkout Code') {
            steps {
                // Clone the GitHub repository
                git url: "${GIT_REPO}", branch: 'master' // Specify the branch if needed
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    // Build the Docker image from the Dockerfile in the repo
                    sh "docker build -t ${DOCKER_IMAGE} ."
                }
            }
        }

        stage('Login to Docker Hub') {
            steps {
                script {
                    // Login to Docker Hub using Jenkins credentials
                    docker.withRegistry('https://index.docker.io/v1/', DOCKER_CREDENTIALS_ID) {
                        // Push the image to Docker Hub
                        sh "docker push ${DOCKER_IMAGE}"
                    }
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