pipeline {
    agent any

    tools {
        // Asegúrate de tener estas herramientas configuradas en Jenkins -> Global Tool Configuration
        maven 'mvn'
        jdk 'jdk11'
    }

    environment {
        DOCKERHUB_USER = 'camilobueno'
        DOCKERHUB_CREDENTIALS = 'pwd'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Spring Boot Services') {
            steps {
                script {
                    def services = ['user-service', 'product-service', 'order-service', 'payment-service', 'api-gateway']

                    for (service in services) {
                        dir("${service}") {
                            bat 'mvn clean package -DskipTests'
                        }
                    }
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    def services = ['user-service', 'product-service', 'order-service', 'payment-service', 'api-gateway']

                    for (service in services) {
                        bat "docker build -t ${DOCKERHUB_USER}/${service}:latest ./${service}"
                    }
                }
            }
        }

        stage('Push Docker Images') {
            steps {
                script {
                    bat "echo ${DOCKERHUB_CREDENTIALS_PSW} | docker login -u ${DOCKERHUB_CREDENTIALS_USR} --password-stdin"

                    def services = ['user-service', 'product-service', 'order-service', 'payment-service', 'api-gateway']

                    for (service in services) {
                        bat "docker push ${DOCKERHUB_USER}/${service}:latest"
                    }
                }
            }
        }
    }
}
