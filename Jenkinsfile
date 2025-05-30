pipeline {
    agent any

    tools {
        maven 'mvn'
        jdk 'jdk11'
    }

    environment {
        DOCKERHUB_USER = 'camilobueno'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Run Unit and Integration Tests') {
            steps {
                script {
                    def services = ['user-service', 'product-service']
                    for (service in services) {
                        dir("${service}") {
                            bat 'mvn test'
                        }
                    }
                }
            }
        }

        stage('Run E2E Tests') {
            steps {
                dir('e2e-tests') { // Ajusta el nombre si tu módulo se llama diferente
                    bat 'mvn test'
                }
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
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKERHUB_USER', passwordVariable: 'DOCKERHUB_PASS')]) {
                        bat """
                            echo %DOCKERHUB_PASS% | docker login -u %DOCKERHUB_USER% --password-stdin
                        """
                        def services = ['user-service', 'product-service', 'order-service', 'payment-service', 'api-gateway']
                        for (service in services) {
                            bat "docker push %DOCKERHUB_USER%/${service}:latest"
                        }
                    }
                }
            }
        }
    }
}
