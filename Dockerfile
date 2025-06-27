FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY target/classes/db/migration /app/classes/db/migration
COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

