FROM maven:3.8.4-openjdk-11-slim AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:11-jre

WORKDIR /app

COPY --from=build /app/target/Docker-app-1.0.0.jar app.jar

EXPOSE 3000

ENTRYPOINT ["java","-jar","app.jar"]
