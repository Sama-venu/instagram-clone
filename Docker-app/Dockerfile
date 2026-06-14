FROM eclipse-temurin:11-jre

WORKDIR /app

COPY target/Docker-app-1.0.0.jar app.jar

EXPOSE 3000

ENTRYPOINT ["java","-jar","app.jar"]
