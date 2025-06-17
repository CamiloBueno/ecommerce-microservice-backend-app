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

    stages{

    stage('compiling services') {
        when{
        anyOf {
                        branch 'dev'
                        branch 'stage'
                        branch 'master'
                    }
                }
                steps {
                    bat 'mvn clean package -DskipTests'
                }
            }

    stage('Unit Tests & Coverage') {
        when { branch 'dev' }
        steps {
            bat """
            @echo off
            set SERVICES=user-service product-service

            for %%S in (%SERVICES%) do (
                echo Running tests and generating coverage for %%S...
                call mvn clean test jacoco:report -pl %%S
            )
            """

            junit '**/target/surefire-reports/*.xml'

            publishHTML(target: [
                reportDir: 'user-service/target/site/jacoco',
                reportFiles: 'index.html',
                reportName: 'Cobertura user-service'
            ])

            publishHTML(target: [
                reportDir: 'product-service/target/site/jacoco',
                reportFiles: 'index.html',
                reportName: 'Cobertura product-service'
            ])
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

        stage('Levantar contenedores para pruebas') {
            steps {
                script {
                    bat '''
                    docker network create ecommerce-test

                    echo Levantando ZIPKIN...
                    docker run -d --name zipkin-container --network ecommerce-test -p 9411:9411 openzipkin/zipkin

                    echo Levantando EUREKA...
                    docker run -d --name service-discovery-container --network ecommerce-test -p 8761:8761 ^
                        -e SPRING_PROFILES_ACTIVE=dev ^
                        -e SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411 ^
                        camilobueno/service-discovery:latest

                    call :waitForService http://localhost:8761/actuator/health

                    echo Levantando CLOUD-CONFIG...
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

                    echo Todos los contenedores están arriba y saludables.
                    exit /b 0

                    :runService
                    set "NAME=%~1"
                    set "PORT=%~2"
                    echo Levantando %NAME%...
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
                                --only-summary
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
                                --only-summary
                        """
                    }
                }
            }
        }

        stage('OWASP ZAP Scan') {
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
        }

        stage('📊 Mostrar URLs de Monitorización') {
            steps {
                echo 'Accede a Prometheus en: http://127.0.0.1:9090'
                echo 'Accede a Grafana en:    http://127.0.0.1:3000'
            }
        }

        stage('Waiting approval for deployment') {
            when { branch 'master' }
            steps {
                input message: 'Approve deployment to production (kubernetes)?', ok: 'Deploy'
            }
        }
    }

    post {
        success {
            echo '✅ Pipeline completed successfully (all stages enabled)'
        }
        failure {
            emailext body: '$DEFAULT_CONTENT', subject: '$DEFAULT_SUBJECT', to: 'camilobueno05@gmail.com'
        }
    }
}
