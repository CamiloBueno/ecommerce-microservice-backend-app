pipeline {
    agent any

    environment {
        DOCKER_HUB_CREDENTIALS = credentials('dockerhub-credentials')
    }

    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/CamiloBueno/ecommerce-microservice-backend-app.git'
            }
        }

        stage('Build JARs') {
            steps {
                bat 'mvn clean package -DskipTests'
            }
        }

        stage('Build and Push Docker Images') {
            steps {
                bat '''
                docker login -u %DOCKER_HUB_CREDENTIALS_USR% -p %DOCKER_HUB_CREDENTIALS_PSW%
                docker build -t camilobueno/api-gateway:latest -f api-gateway/Dockerfile .
                docker push camilobueno/api-gateway:latest
                '''
            }
        }

        stage('Deploy to Minikube') {
            steps {
                bat 'kubectl apply -f k8s/'
            }
        }
    }
}
