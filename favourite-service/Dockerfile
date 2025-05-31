FROM openjdk:11
#ARG PROJECT_VERSION=0.1.0
RUN mkdir -p /home/app
WORKDIR /home/app
#COPY api-gateway/ .
COPY target/favourite-service-v0.1.0.jar favourite-service.jar
EXPOSE 8800
ENTRYPOINT ["java", "-jar", "favourite-service.jar"]


