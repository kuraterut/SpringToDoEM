# Используем официальный образ OpenJDK
FROM eclipse-temurin:21-jdk-jammy

# Рабочая директория в контейнере
WORKDIR /app

# Копируем собранный JAR файл в контейнер
COPY target/classes/db/migration /app/classes/db/migration
COPY target/*.jar app.jar

# Открываем порт, на котором работает приложение
EXPOSE 8080

# Команда для запуска приложения
ENTRYPOINT ["java", "-jar", "app.jar"]