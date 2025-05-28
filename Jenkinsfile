pipeline {
    agent any

    environment {
        DOCKER_HUB_CREDENTIALS = credentials('dockerhub-credentials')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build with Maven') {
            steps {
                bat 'mvn clean package -DskipTests'
            }
        }

        stage('Build and Push Docker Images') {
            steps {
                script {
                    def services = [
                        'api-gateway',
                        'cloud-config',
                        'order-service',
                        'payment-service',
                        'product-service',
                        'user-service'
                    ]

                    for (service in services) {
                        bat """
                            cd ${service}
                            docker build -t camilobueno/${service}:latest .
                            echo ${DOCKER_HUB_CREDENTIALS_PSW} | docker login -u ${DOCKER_HUB_CREDENTIALS_USR} --password-stdin
                            docker push camilobueno/${service}:latest
                            cd ..
                        """
                    }
                }
            }
        }

        stage('Deploy to Minikube') {
            steps {
                bat 'kubectl apply -f k8s/'
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed.'
        }
    }
}
