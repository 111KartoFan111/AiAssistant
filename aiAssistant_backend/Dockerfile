# Используем официальный образ Maven с Java 17 для сборки
FROM maven:3.9.5-eclipse-temurin-17 AS build

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем pom.xml и загружаем зависимости (для кеширования слоёв)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходный код
COPY src ./src

# Собираем приложение
RUN mvn clean package -DskipTests

# Используем лёгкий образ JRE для запуска
FROM eclipse-temurin:17-jre

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем JAR файл из стадии сборки
COPY --from=build /app/target/*.jar app.jar

# Копируем Firebase credentials (если есть)
COPY src/main/resources/serviceAccountKey.json /app/serviceAccountKey.json

# Открываем порт
EXPOSE 8080

# Переменные окружения (можно переопределить при запуске)
ENV SPRING_PROFILES_ACTIVE=prod

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "/app/app.jar"]