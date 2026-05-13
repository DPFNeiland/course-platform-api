# Etapa 1: build da aplicação
FROM maven:3.9.12-eclipse-temurin-25 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: imagem final enxuta
FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
