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

        // stage('Checkout') {
        //     steps {
        //         checkout scm
        //     }
        // }

        // stage('Verify Tools') {
        //     steps {
        //         bat 'java -version'
        //         bat 'mvn -version'
        //         bat 'docker --version'
        //         bat 'kubectl config current-context --kubeconfig=%KUBECONFIG%'
        //     }
        // }

        // stage('Build Services (creating .jar files)') {
        //     steps {
        //         bat 'mvn clean package -DskipTests'
        //     }
        // }

        // stage('Unit Tests') {
        //     steps {
        //         script {
        //             ['user-service', 'product-service'].each {
        //                 dir(it) {
        //                     bat 'mvn test'
        //                 }
        //             }
        //         }
        //     }
        // }

        // stage('Integration Tests') {
        //     steps {
        //         script {
        //             ['user-service', 'product-service'].each {
        //                 dir(it) {
        //                     bat 'mvn verify'
        //                 }
        //             }
        //         }
        //     }
        // }

        // stage('E2E Tests') {
        //     steps {
        //         bat 'mvn verify -pl e2e'
        //     }
        // }

        // stage('Build Docker Images of each service') {
        //     steps {
        //         script {
        //             SERVICES.split().each { service ->
        //                 bat "docker build -t %DOCKERHUB_USER%/${service}:latest ./${service}"
        //             }
        //         }
        //     }
        // }

        // stage('Push Docker Images to Docker Hub') {
        //     steps {
        //         withCredentials([usernamePassword(credentialsId: "${DOCKER_CREDENTIALS_ID}", usernameVariable: 'DOCKERHUB_USER', passwordVariable: 'DOCKERHUB_PASS')]) {
        //             bat "docker login -u %DOCKERHUB_USER% -p %DOCKERHUB_PASS%"
        //             script {
        //                 SERVICES.split().each { service ->
        //                     bat "docker push %DOCKERHUB_USER%/${service}:latest"
        //                 }
        //             }
        //         }
        //     }
        // }

        stage('Start containers for load and stress testing') {
            //when { branch 'stage' }
            steps {
                script {
                    bat '''
                    docker network create ecommerce-test || true

                    docker run -d --name zipkin-container --network ecommerce-test -p 9411:9411 openzipkin/zipkin

                    docker run -d --name service-discovery-container --network ecommerce-test -p 8761:8761 ^
                    -e SPRING_PROFILES_ACTIVE=stage ^
                    -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 ^
                    jacoboossag/service-discovery:${IMAGE_TAG}

                    until curl -s http://localhost:8761/actuator/health | findstr /C:"\"status\":\"UP\"" > nul; do
                        echo "Waiting for service discovery to be ready..."
                        timeout /t 10
                    done

                    docker run -d --name cloud-config-container --network ecommerce-test -p 9296:9296 ^
                    -e SPRING_PROFILES_ACTIVE=stage ^
                    -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 ^
                    -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://service-discovery-container:8761/eureka/ ^
                    -e EUREKA_INSTANCE=cloud-config-container ^
                    jacoboossag/cloud-config:${IMAGE_TAG}

                    until curl -s http://localhost:9296/actuator/health | findstr /C:"\"status\":\"UP\"" > nul; do
                        echo "Waiting for cloud config to be ready..."
                        timeout /t 10
                    done

                    docker run -d --name order-service-container --network ecommerce-test -p 8300:8300 ^
                    -e SPRING_PROFILES_ACTIVE=stage ^
                    -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 ^
                    -e SPRING_CONFIG_IMPORT=optional:configserver:http://cloud-config-container:9296 ^
                    -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://service-discovery-container:8761/eureka ^
                    -e EUREKA_INSTANCE=order-service-container ^
                    jacoboossag/order-service:${IMAGE_TAG}

                    until curl -s http://localhost:8300/order-service/actuator/health | findstr /C:"\"status\":\"UP\"" > nul; do
                        echo "Waiting for order service to be ready..."
                        timeout /t 10
                    done

                    docker run -d --name payment-service-container --network ecommerce-test -p 8400:8400 ^
                    -e SPRING_PROFILES_ACTIVE=stage ^
                    -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 ^
                    -e SPRING_CONFIG_IMPORT=optional:configserver:http://cloud-config-container:9296 ^
                    -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://service-discovery-container:8761/eureka ^
                    -e EUREKA_INSTANCE=payment-service-container ^
                    jacoboossag/payment-service:${IMAGE_TAG}

                    until curl -s http://localhost:8400/payment-service/actuator/health | findstr /C:"\"status\":\"UP\"" > nul; do
                        echo "Waiting for payment service to be ready..."
                        timeout /t 10
                    done

                    docker run -d --name product-service-container --network ecommerce-test -p 8500:8500 ^
                    -e SPRING_PROFILES_ACTIVE=stage ^
                    -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 ^
                    -e SPRING_CONFIG_IMPORT=optional:configserver:http://cloud-config-container:9296 ^
                    -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://service-discovery-container:8761/eureka ^
                    -e EUREKA_INSTANCE=product-service-container ^
                    jacoboossag/product-service:${IMAGE_TAG}

                    until curl -s http://localhost:8500/product-service/actuator/health | findstr /C:"\"status\":\"UP\"" > nul; do
                        echo "Waiting for product service to be ready..."
                        timeout /t 10
                    done

                    docker run -d --name shipping-service-container --network ecommerce-test -p 8600:8600 ^
                    -e SPRING_PROFILES_ACTIVE=stage ^
                    -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 ^
                    -e SPRING_CONFIG_IMPORT=optional:configserver:http://cloud-config-container:9296 ^
                    -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://service-discovery-container:8761/eureka ^
                    -e EUREKA_INSTANCE=shipping-service-container ^
                    jacoboossag/shipping-service:${IMAGE_TAG}

                    until curl -s http://localhost:8600/shipping-service/actuator/health | findstr /C:"\"status\":\"UP\"" > nul; do
                        echo "Waiting for shipping service to be ready..."
                        timeout /t 10
                    done

                    docker run -d --name user-service-container --network ecommerce-test -p 8700:8700 ^
                    -e SPRING_PROFILES_ACTIVE=stage ^
                    -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 ^
                    -e SPRING_CONFIG_IMPORT=optional:configserver:http://cloud-config-container:9296 ^
                    -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://service-discovery-container:8761/eureka ^
                    -e EUREKA_INSTANCE=user-service-container ^
                    jacoboossag/user-service:${IMAGE_TAG}

                    until curl -s http://localhost:8700/user-service/actuator/health | findstr /C:"\"status\":\"UP\"" > nul; do
                        echo "Waiting for user service to be ready..."
                        timeout /t 10
                    done

                    docker run -d --name favourite-service-container --network ecommerce-test -p 8800:8800 ^
                    -e SPRING_PROFILES_ACTIVE=stage ^
                    -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 ^
                    -e SPRING_CONFIG_IMPORT=optional:configserver:http://cloud-config-container:9296 ^
                    -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://service-discovery-container:8761/eureka ^
                    -e EUREKA_INSTANCE=favourite-service-container ^
                    jacoboossag/favourite-service:${IMAGE_TAG}

                    until curl -s http://localhost:8800/favourite-service/actuator/health | findstr /C:"\"status\":\"UP\"" > nul; do
                        echo "Waiting for favourite service to be ready..."
                        timeout /t 10
                    done
                    '''
                }
            }
        }


        stage('Run Load Tests with Locust') {
            steps {
                script {
                    def locustTests = [
                        [name: 'order-service', container: 'order-service-container', port: 8300],
                        [name: 'payment-service', container: 'payment-service-container', port: 8400]
                    ]

                    for (test in locustTests) {
                        bat """
                            docker run --rm --network ecommerce-test -v %cd%/locust/test:/mnt/locust locustio/locust ^
                            -f /mnt/locust/${test.name}/locustfile.py ^
                            --host=http://${test.container}:${test.port} --headless -u 10 -r 2 -t 30s
                        """
                    }
                }
            }
        }

        stage('Run Stress Tests with Locust') {
            steps {
                script {
                    def stressTests = [
                        [name: 'order-service', container: 'order-service-container', port: 8300],
                        [name: 'payment-service', container: 'payment-service-container', port: 8400]
                    ]

                    for (test in stressTests) {
                        bat """
                            docker run --rm --network ecommerce-test -v %cd%/locust/test:/mnt/locust locustio/locust ^
                            -f /mnt/locust/${test.name}/locustfile.py ^
                            --host=http://${test.container}:${test.port} --headless -u 50 -r 5 -t 1m
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
