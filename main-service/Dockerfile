FROM openjdk:17-jdk-slim

WORKDIR /app

COPY build/libs/main-service-0.0.1-SNAPSHOT.jar main-service.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "main-service.jar"]
