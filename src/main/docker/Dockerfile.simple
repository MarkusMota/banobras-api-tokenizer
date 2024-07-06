FROM eclipse-temurin:17.0.11_9-jdk-alpine

VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]