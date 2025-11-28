FROM eclipse-temurin:24-jre

LABEL description="API Gateway providing routing and authentication for AbernathyClinic Application"
WORKDIR /app
EXPOSE 8080

COPY ./target/abernathyclinic-gateway-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar" ]