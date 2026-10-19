# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/booking-service-0.0.1-SNAPSHOT.jar app.jar

# Non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
