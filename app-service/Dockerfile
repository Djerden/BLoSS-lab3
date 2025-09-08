FROM openjdk:17-jdk-slim

WORKDIR /app

COPY build/libs/app-service-0.0.1-SNAPSHOT.jar app-service.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app-service.jar"]