pipeline {
    agent any

    tools {
        maven 'mvn'
        jdk 'jdk11'
    }

    environment {
        DOCKERHUB_USER = 'camilobueno'
        DOCKER_CREDENTIALS_ID = 'dockerhub-credentials'
        KUBECONFIG = 'C:\\Users\\camil\\.kube\\config'
        SERVICES = 'api-gateway cloud-config order-service payment-service product-service proxy-client service-discovery shipping-service user-service'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Verify Tools') {
            steps {
                bat 'java -version'
                bat 'mvn -version'
                bat 'docker --version'
                bat 'kubectl config current-context --kubeconfig=%KUBECONFIG%'
            }
        }

        stage('Build Services (creating .jar files)') {
            steps {
                bat 'mvn clean package -DskipTests'
            }
        }

        stage('Unit Tests') {
            steps {
                script {
                    ['user-service', 'product-service'].each {
                        dir(it) {
                            bat 'mvn test'
                        }
                    }
                }
            }
        }

        stage('Integration Tests') {
            steps {
                script {
                    ['user-service', 'product-service'].each {
                        dir(it) {
                            bat 'mvn verify'
                        }
                    }
                }
            }
        }

        stage('E2E Tests') {
            steps {
                bat 'mvn verify -pl e2e'
            }
        }

        stage('Build Docker Images of each service') {
            steps {
                script {
                    SERVICES.split().each { service ->
                        bat "docker build -t %DOCKERHUB_USER%/${service}:latest ./${service}"
                    }
                }
            }
        }

        stage('Push Docker Images to Docker Hub') {
            steps {
                withCredentials([usernamePassword(credentialsId: "${DOCKER_CREDENTIALS_ID}", usernameVariable: 'DOCKERHUB_USER', passwordVariable: 'DOCKERHUB_PASS')]) {
                    bat "docker login -u %DOCKERHUB_USER% -p %DOCKERHUB_PASS%"
                    script {
                        SERVICES.split().each { service ->
                            bat "docker push %DOCKERHUB_USER%/${service}:latest"
                        }
                    }
                }
            }
        }

        stage('Start containers for load and stress testing') {
            steps {
                script {
                    bat "docker network inspect ecommerce-test || docker network create ecommerce-test"

                    def services = [
                        [name: 'zipkin', port: 9411, image: 'openzipkin/zipkin', health: 'http://localhost:9411'],
                        [name: 'service-discovery-container', port: 8761, image: 'camilobueno/service-discovery:latest', health: 'http://localhost:8761/actuator/health'],
                        [name: 'cloud-config-container', port: 9296, image: 'camilobueno/cloud-config:latest', health: 'http://localhost:9296/cloud-config/actuator/health'],
                        [name: 'order-service-container', port: 8300, image: 'camilobueno/order-service:latest', health: 'http://localhost:8300/order-service/actuator/health'],
                        [name: 'payment-service-container', port: 8400, image: 'camilobueno/payment-service:latest', health: 'http://localhost:8400/payment-service/actuator/health']
                    ]

                    for (svc in services) {
                        bat "docker run -d --rm --network ecommerce-test -p ${svc.port}:${svc.port} --name ${svc.name} ${svc.image}"
                        bat '''
                            powershell -Command "$i=0; while ($i -lt 30) {
                                try {
                                    $resp = Invoke-WebRequest -Uri '${svc.health}' -UseBasicParsing -TimeoutSec 3;
                                    if ($resp.StatusCode -eq 200) {
                                        Write-Host '${svc.name} is healthy';
                                        break
                                    }
                                } catch {}
                                Start-Sleep -Seconds 2;
                                $i++
                            }"
                        '''
                    }
                }
            }
        }

        stage('Run Load Tests with Locust') {
            steps {
                script {
                    def locustTests = [
                        [name: 'order-service', port: 8300],
                        [name: 'payment-service', port: 8400]
                    ]

                    for (test in locustTests) {
                        bat """
                            docker run --rm --network ecommerce-test -v %cd%/locust/test:/mnt/locust locustio/locust ^
                            -f /mnt/locust/${test.name}/locustfile.py ^
                            --host=http://localhost:${test.port} --headless -u 10 -r 2 -t 30s
                        """
                    }
                }
            }
        }

        stage('Run Stress Tests with Locust') {
            steps {
                script {
                    def stressTests = [
                        [name: 'order-service', port: 8300],
                        [name: 'payment-service', port: 8400]
                    ]

                    for (test in stressTests) {
                        bat """
                            docker run --rm --network ecommerce-test -v %cd%/locust/test:/mnt/locust locustio/locust ^
                            -f /mnt/locust/${test.name}/locustfile.py ^
                            --host=http://localhost:${test.port} --headless -u 50 -r 5 -t 1m
                        """
                    }
                }
            }
        }
    }

    post {
        success {
            echo '✅ Pipeline completed successfully (until Locust tests)'
        }
    }
}
