FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY target/final-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]