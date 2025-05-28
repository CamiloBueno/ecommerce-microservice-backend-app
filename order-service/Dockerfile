
FROM openjdk:11
#ARG PROJECT_VERSION=0.1.0
RUN mkdir -p /home/app
WORKDIR /home/app
#COPY order-service/ .
COPY target/order-service-v0.1.0.jar order-service.jar
EXPOSE 8300
ENTRYPOINT ["java", "-jar", "order-service.jar"]

