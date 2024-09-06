pipeline {
    agent any 
    
    stages { 
        stage('SCM Checkout') {
            steps{
           git branch: 'master', url: 'https://github.com/XP2600-hub/nhorizon-java-container.git'
            }
        }
        // run sonarqube test
        stage('Run Sonarqube') {
            environment {
                scannerHome = tool 'Sonarscanner';
            }
            steps {
              withSonarQubeEnv(credentialsId: 'XP_2600', installationName: 'sona') {
                sh "${scannerHome}/bin/sonar-scanner"
              }
            }
        }
    }
}