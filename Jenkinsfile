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

    stage('Scanning Branch') {
        steps {
            bat """
            @echo off
            setlocal EnableDelayedExpansion

            echo Detected branch: %BRANCH_NAME%

            REM Simular diccionario profileConfig
            set SPRING_PROFILES_ACTIVE=dev
            set IMAGE_TAG=dev
            set DEPLOYMENT_SUFFIX=-dev

            if "%BRANCH_NAME%"=="master" (
                set SPRING_PROFILES_ACTIVE=prod
                set IMAGE_TAG=prod
                set DEPLOYMENT_SUFFIX=-prod
            ) else if "%BRANCH_NAME%"=="stage" (
                set SPRING_PROFILES_ACTIVE=stage
                set IMAGE_TAG=stage
                set DEPLOYMENT_SUFFIX=-stage
            )

            REM Flags
            if "%BRANCH_NAME%"=="master" (
                set IS_MASTER=true
            ) else (
                set IS_MASTER=false
            )

            if "%BRANCH_NAME%"=="stage" (
                set IS_STAGE=true
            ) else (
                set IS_STAGE=false
            )

            if "%BRANCH_NAME%"=="dev" (
                set IS_DEV=true
            ) else (
                set IS_DEV=false
            )

            REM startsWith simulation for 'feature/'
            set "IS_FEATURE=false"
            echo %BRANCH_NAME% | findstr /B "feature/" >nul
            if !errorlevel! == 0 (
                set IS_FEATURE=true
            )

            echo Spring profile: !SPRING_PROFILES_ACTIVE!
            echo Image tag: !IMAGE_TAG!
            echo Deployment suffix: !DEPLOYMENT_SUFFIX!
            echo Flags: IS_MASTER=!IS_MASTER!, IS_STAGE=!IS_STAGE!, IS_DEV=!IS_DEV!, IS_FEATURE=!IS_FEATURE!
            """
        }
    }


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


/*stage('Run SonarQube Analysis   ') {
    tools {
        jdk 'jdk-17'
    }
    environment {
        JAVA_HOME = tool 'jdk-17'
        PATH = "${JAVA_HOME}\\bin;${env.PATH}"
        scannerHome = tool 'SonarQubeInstall'
    }
    steps {
        script {
            def javaServices = [
                'api-gateway',
                'cloud-config',
                'favourite-service',
                'order-service',
                'payment-service',
                'product-service',
                'proxy-client',
                'service-discovery',
                'shipping-service',
                'user-service',
                'e2e'
            ]

            withSonarQubeEnv(credentialsId: 'SonarQubeC', installationName: 'SonarQubeServerInstall') {
                javaServices.each { service ->
                    dir(service) {
                        bat "\"${scannerHome}\\bin\\sonar-scanner.bat\" " +
                            "-Dsonar.projectKey=${service} " +
                            "-Dsonar.projectName=${service} " +
                            "-Dsonar.sources=src " +
                            "-Dsonar.java.binaries=target\\classes"
                    }
                }

                dir('locust') {
                    bat "\"${scannerHome}\\bin\\sonar-scanner.bat\" " +
                        "-Dsonar.projectKey=locust " +
                        "-Dsonar.projectName=locust " +
                        "-Dsonar.sources=test"
                }
            }
        }
    }
}*/
/*
stage('Trivy Vulnerability Scan & Report') {
    environment{
        TRIVY_DIR = "C:\\Users\\camil\\Downloads\\trivy_0.63.0_windows-64bit"
    }
    steps {
        script {
            env.PATH = "${TRIVY_DIR};${env.PATH}"
            def services = [
                'api-gateway',
                'cloud-config',
                'favourite-service',
                'order-service',
                'payment-service',
                'product-service',
                'proxy-client',
                'service-discovery',
                'shipping-service',
                'user-service'
            ]

            // Crear carpeta trivy-reports
            bat '''
            if exist trivy-reports rmdir /s /q trivy-reports
            mkdir trivy-reports
            '''

            // Ejecutar trivy por cada imagen
            services.each { service ->
                def reportPath = "trivy-reports\\${service}.html"
                echo "🔍 Scanning image ${DOCKERHUB_USER}/${service}:latest with Trivy..."

                bat """
                trivy image --format template ^
                    --template="@C:/Users/camil/Downloads/trivy_0.63.0_windows-64bit/contrib/html.tpl" ^
                    --severity HIGH,CRITICAL ^
                    -o ${reportPath} ^
                    ${DOCKERHUB_USER}/${service}:latest
                """
            }

            // Publicar los reportes en HTML
            publishHTML(target: [
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'trivy-reports',
                reportFiles: '*.html',
                reportName: 'Trivy Scan Report'
            ])
        }
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
*/ /*
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
                }*/
/*
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
                                                --host http://${test.container}:${test.port} ^
                                                --headless -u 50 -r 5 -t 1m ^
                                                --only-summary ^
                        """
                    }
                }
            }
        }

        stage('Waiting approval for deployment') {
                    when { branch 'master' }
                    steps {
                        script {


                            input message: 'Approve deployment to production (kubernetes) ?', ok: 'Deploy'
                        }
                    }
                }
    }


*/
/*
        stage('OWASP ZAP Scan') {
            when { branch 'master' }
            steps {
                script {
                    echo '🔐 Iniciando escaneos con OWASP ZAP en Windows...'

                    def targets = [
                        [name: 'order-service', url: 'http://order-service-container:8300/order-service'],
                        [name: 'payment-service', url: 'http://payment-service-container:8400/payment-service'],
                        [name: 'product-service', url: 'http://product-service-container:8500/product-service'],
                        [name: 'shipping-service', url: 'http://shipping-service-container:8600/shipping-service'],
                        [name: 'user-service', url: 'http://user-service-container:8700/user-service'],
                        [name: 'favourite-service', url: 'http://favourite-service-container:8800/favourite-service']
                    ]

                    // Crear carpeta si no existe
                    bat '''
                    if not exist zap-reports (
                        mkdir zap-reports
                    )
                    '''

                    targets.each { service ->
                        def reportFile = "zap-reports\\report-${service.name}.html"
                        echo "🚨 Escaneando ${service.name} (${service.url}) con ZAP..."

                        bat """
                        docker run --rm ^
                            --network ecommerce-test ^
                            -v "%CD%\\zap-reports:/zap/wrk" ^
                            zaproxy/zap-stable zap-full-scan.py ^
                            -t ${service.url} ^
                            -r report-${service.name}.html ^
                            -I
                        """
                    }

                    // Publicar los reportes HTML en Jenkins
                    publishHTML(target: [
                        allowMissing: true,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'zap-reports',
                        reportFiles: '*.html',
                        reportName: 'ZAP Full Scan Reports'
                    ])
                }
            }
        }*/
/*
    stage('Deploy Monitoring Stack') {
        steps {
            script {
                echo "🚀 Desplegando Prometheus y Grafana con dashboards automáticos..."

                // Crear o actualizar los ConfigMaps necesarios
                bat 'kubectl create configmap grafana-datasources --from-file=monitoring/grafana-datasource.yaml --dry-run=client -o yaml | kubectl apply -f -'
                bat 'kubectl create configmap grafana-dashboard-config --from-file=monitoring/grafana-dashboard.yaml --dry-run=client -o yaml | kubectl apply -f -'
                bat 'kubectl create configmap grafana-dashboard-json --from-file=monitoring/dashboards/node-exporter-full.json --dry-run=client -o yaml | kubectl apply -f -'

                // Aplicar Prometheus y Grafana
                bat 'kubectl apply -f monitoring\\prometheus-config.yaml'
                bat 'kubectl apply -f monitoring\\prometheus-deployment.yaml'
                bat 'kubectl apply -f monitoring\\prometheus-service.yaml'
                bat 'kubectl apply -f monitoring\\grafana-deployment.yaml'
                bat 'kubectl apply -f monitoring\\grafana-service.yaml'

                echo "✅ Monitoring desplegado."

                // Mostrar las URLs locales para acceder a los servicios
                echo "🌐 Accede a Prometheus en: http://localhost:32478"
                echo "📊 Accede a Grafana en: http://localhost:31950 (usa 'minikube service grafana --url' si no carga)"
            }
        }
    }

    stage('📊 Mostrar URLs de Monitorización') {
        steps {
            echo 'Accede a Prometheus en: http://127.0.0.1:9090'
            echo 'Accede a Grafana en:    http://127.0.0.1:3000'
        }
    }
    */
   /* post {
                success {
                    echo '✅ Pipeline completed successfully (until Locust tests)'
                }
                failure {
                    emailext body: '$DEFAULT_CONTENT', subject: '$DEFAULT_SUBJECT', to: 'camilobueno05@gmail.com'
                }
            }*/

  }

}
