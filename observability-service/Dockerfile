FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn dependency:go-offline -B -q

COPY src/ src/
RUN mvn package -DskipTests -B -q

# Extract Spring Boot layered JAR for optimised image layers
RUN java -Djarmode=layertools     -jar target/*.jar     extract --destination target/extracted

FROM eclipse-temurin:21-jre-alpine AS runtime

# Non-root user — required for AKS pod security
RUN addgroup -S gtbs && adduser -S gtbs -G gtbs
USER gtbs

WORKDIR /app

COPY --from=build /workspace/target/extracted/dependencies/          ./
COPY --from=build /workspace/target/extracted/spring-boot-loader/    ./
COPY --from=build /workspace/target/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/target/extracted/application/           ./

HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

# Container-aware JVM flags:
#   UseContainerSupport — honour cgroup memory limits
#   MaxRAMPercentage    — use 75% of container memory for heap
#   ExitOnOutOfMemoryError — crash fast so K8s can restart cleanly

ENV JVM_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

ENTRYPOINT ["sh", "-c", "java $JVM_OPTS org.springframework.boot.loader.launch.JarLauncher"]
