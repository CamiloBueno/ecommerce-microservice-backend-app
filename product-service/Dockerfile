
FROM openjdk:11
#ARG PROJECT_VERSION=0.1.0
RUN mkdir -p /home/app
WORKDIR /home/app
#COPY product-service/ .
COPY target/product-service-v0.1.0.jar product-service.jar
EXPOSE 8500
ENTRYPOINT ["java", "-jar", "product-service.jar"]


