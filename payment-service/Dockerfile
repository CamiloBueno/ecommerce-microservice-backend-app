
FROM openjdk:11
#ARG PROJECT_VERSION=0.1.0
RUN mkdir -p /home/app
WORKDIR /home/app
#COPY payment-service/ .
COPY target/payment-service-v0.1.0.jar payment-service.jar
EXPOSE 8400
ENTRYPOINT ["java", "-jar", "payment-service.jar"]


