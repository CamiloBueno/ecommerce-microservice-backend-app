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
                withCredentials([usernamePassword(credentialsId: "${DOCKER_CREDENTIALS_ID}", usernameVariable: 'DOCKERHUB_USER', passwordVariable: 'DOCKERHUB_PASS')])
                {
                    bat "docker login -u %DOCKERHUB_USER% -p %DOCKERHUB_PASS%"
                    script {
                        SERVICES.split().each { service ->
                        bat "docker push %DOCKERHUB_USER%/${service}:latest"
                        }
                    }
                }
            }
        }

        stage('Levantar contenedores para pruebas') {
                    //when {
                      //  anyOf {
                      //      branch 'stage'
                      //  }
                    //}
                    steps {
                        script {
                            bat '''

                            docker network create ecommerce-test

                            echo  Levantando ZIPKIN...
                            docker run -d --name zipkin-container --network ecommerce-test -p 9411:9411 openzipkin/zipkin

                            echo  Levantando EUREKA...
                            docker run -d --name service-discovery-container --network ecommerce-test -p 8761:8761 ^
                                -e SPRING_PROFILES_ACTIVE=dev ^
                                -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 ^
                                camilobueno/service-discovery:latest

                            call :waitForService http://localhost:8761/actuator/health

                            echo  Levantando CLOUD-CONFIG...
                            docker run -d --name cloud-config-container --network ecommerce-test -p 9296:9296 ^
                                -e SPRING_PROFILES_ACTIVE=dev ^
                                -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 ^
                                -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://service-discovery-container:8761/eureka/ ^
                                -e EUREKA_INSTANCE=cloud-config-container ^
                                camilobueno/cloud-config:latest

                            call :waitForService http://localhost:9296/actuator/health

                            call :runService order-service 8300
                            call :runService payment-service 8400
                            call :runService product-service 8500
                            call :runService shipping-service 8600
                            call :runService user-service 8700
                            call :runService favourite-service 8800

                            echo  Todos los contenedores están arriba y saludables.
                            exit /b 0

                            :runService
                            set "NAME=%~1"
                            set "PORT=%~2"
                            echo  Levantando %NAME%...
                            docker run -d --name %NAME%-container --network ecommerce-test -p %PORT%:%PORT% ^
                                -e SPRING_PROFILES_ACTIVE=dev ^
                                -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 ^
                                -e SPRING_CONFIG_IMPORT=optional:configserver:http://cloud-config-container:9296 ^
                                -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://service-discovery-container:8761/eureka ^
                                -e EUREKA_INSTANCE=%NAME%-container ^
                                camilobueno/%NAME%:latest
                            call :waitForService http://localhost:%PORT%/%NAME%/actuator/health
                            exit /b 0

                            :waitForService
                            set "URL=%~1"
                            echo ⏳ Esperando a que %URL% esté disponible...
                            :wait_loop
                            for /f "delims=" %%i in ('curl -s %URL% ^| jq -r ".status"') do (
                                if "%%i"=="UP" goto :eof
                            )
                            timeout /t 5 /nobreak
                            goto wait_loop
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
                            docker run --rm --network ecommerce-test ^
                                                -v "%CD%\\locust:/mnt" ^
                                                camilobueno/locust:latest ^
                                                -f /mnt/test/${test.name}/locustfile.py ^
                                                --host http://${test.container}:${test.port} ^
                                                --headless -u 10 -r 2 -t 1m ^
                                                --only-summary ^
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
                            docker run --rm --network ecommerce-test ^
                            -v "%CD%\\locust:/mnt" ^
                            camilobueno/locust:latest ^
                            -f /mnt/test/${test.name}/locustfile.py ^
                            --host=http://${test.container}:${test.port} ^
                            --headless -u 50 -r 5 -t 1m ^
                            --only-summary ^

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
