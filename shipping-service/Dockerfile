FROM openjdk:11
#ARG PROJECT_VERSION=0.1.0
RUN mkdir -p /home/app
WORKDIR /home/app
#COPY api-gateway/ .
COPY target/shipping-service-v0.1.0.jar shipping-service.jar
EXPOSE 8600
ENTRYPOINT ["java", "-jar", "shipping-service.jar"]




