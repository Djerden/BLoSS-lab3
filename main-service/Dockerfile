FROM openjdk:17-jdk-slim

WORKDIR /app

COPY build/libs/Lab1-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

#FROM openjdk:17-jdk-slim AS build
#
#WORKDIR /app
#
#COPY . .
#
#RUN chmod +x gradlew
#
#RUN ./gradlew build --no-daemon
#
#FROM openjdk:17-jdk-slim
#
#WORKDIR /app
#
#COPY --from=build /app/build/libs/backend-0.0.1-SNAPSHOT.jar app.jar
#
#EXPOSE 8080
#
#ENTRYPOINT ["java", "-jar", "app.jar"]