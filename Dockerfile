FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY target/aws-finops-mcp.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]