node {
    // Environment variables
    def sonarQubeUrl = 'http://<sonarqube_host>:<port>'
    def sonarQubeToken = credentials('sonarqube-token') // Use Jenkins credentials for security
    def dockerImage = 'dockerhub_user/image_name:tag'

    try {
        stage('Pull Docker Image') {
            // Pull the Docker image from Docker Hub
            sh "docker pull ${dockerImage}"
        }

        stage('SonarQube Scan') {
            // Run SonarQube scanner inside the Docker container
            sh """
            docker run --rm \
                -e SONAR_HOST_URL=${sonarQubeUrl} \
                -e SONAR_TOKEN=${sonarQubeToken} \
                -v \$(pwd):/usr/src \
                -w /usr/src \
                sonarsource/sonar-scanner-cli \
                -Dsonar.projectKey=myProjectKey \
                -Dsonar.sources=. \
                -Dsonar.docker.image=${dockerImage}
            """
        }
    } catch (Exception e) {
        // Handle any errors that occurred during the pipeline
        currentBuild.result = 'FAILURE'
        error('Pipeline failed: ' + e.message)
    } finally {
        echo 'Pipeline completed.'
    }
}