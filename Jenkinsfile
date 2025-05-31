pipeline {
    agent any

    tools {
        maven 'mvn'
        jdk 'jdk11'
    }

    environment {
        DOCKERHUB_USER = 'camilobueno'
        KUBECONFIG = 'C:\\Users\\camil\\.kube\\config'
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
                    def services = ['user-service', 'product-service', 'payment-service', 'order-service']
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
                bat 'mvn verify -pl e2e'
            }
        }

        stage('Build Spring Boot Services') {
            steps {
                bat 'mvn clean package -DskipTests'
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    def services = [
                        'service-discovery',
                        'cloud-config',
                        'api-gateway',
                        'proxy-client',
                        'order-service',
                        'payment-service',
                        'product-service',
                        'shipping-service',
                        'user-service',
                        'favourite-service'
                    ]
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
                        bat "echo %DOCKERHUB_PASS% | docker login -u %DOCKERHUB_USER% --password-stdin"
                        def services = [
                            'service-discovery',
                            'cloud-config',
                            'api-gateway',
                            'proxy-client',
                            'order-service',
                            'payment-service',
                            'product-service',
                            'shipping-service',
                            'user-service',
                            'favourite-service'
                        ]
                        for (service in services) {
                            bat "docker push %DOCKERHUB_USER%/${service}:latest"
                        }
                    }
                }
            }
        }

        stage('Deploy to Minikube') {
            steps {
                script {
                    def yamls = [
                        'zipkin-kube',
                        'service-discovery-kube',
                        'cloud-config-kube',
                        'api-gateway-kube',
                        'order-service-kube',
                        'payment-service-kube',
                        'product-service-kube',
                        'user-service-kube'
                    ]
                    for (service in yamls) {
                        bat "kubectl apply -f k8s/${service}/ --kubeconfig=%KUBECONFIG%"
                    }
                }
            }
        }

        stage('Run Locust Load Tests') {
            steps {
                script {
                    bat "docker network create locust-net"

                    def services = [
                        'zipkin',
                        'service-discovery',
                        'cloud-config',
                        'api-gateway',
                        'proxy-client',
                        'order-service',
                        'payment-service',
                        'product-service',
                        'shipping-service',
                        'user-service',
                        'favourite-service'
                    ]
                    for (service in services) {
                        bat "docker run -d --rm --network locust-net --name ${service}-test ${DOCKERHUB_USER}/${service}:latest"
                        echo "Esperando 10 segundos para que ${service}-test esté listo..."
                        bat "timeout /T 10 /NOBREAK"
                    }

                    def targets = ['api-gateway', 'product-service']
                    for (target in targets) {
                        bat """
                            docker run --rm --network locust-net -v %cd%/locust:/mnt/locust locustio/locust ^
                            -f /mnt/locust/${target}/locustfile.py --headless -u 10 -r 2 ^
                            --host=http://${target}-test --run-time 30s
                        """
                    }

                    for (service in services) {
                        bat "docker stop ${service}-test || exit 0"
                    }

                    bat "docker network rm locust-net"
                }
            }
        }
    }
}
