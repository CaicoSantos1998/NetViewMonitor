FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
LABEL authors="caicosantos1998"
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
VOLUME /app/logs
ENTRYPOINT ["java", "-jar", "app.jar"]