### Builder
FROM ghcr.io/graalvm/native-image-community:25.0.1 AS build

# Copy
WORKDIR /app
COPY pom.xml mvnw ./
COPY src ./src
COPY .mvn/ ./.mvn

# Build
RUN ./mvnw -Pnative package -DskipTests -Dskip.unit.tests=true

### Deployer
FROM gcr.io/distroless/java-base:nonroot AS deploy

# Copy
WORKDIR /app
COPY --from=build /app/target/nr-forest-client-legacy ./nr-forest-client-legacy

# User, port and health check
USER 1001
EXPOSE 9000
HEALTHCHECK CMD curl -f http://localhost:9000/actuator/health | grep '"status":"UP"'

ENV SPRING_PROFILES_ACTIVE=container

# Startup
ENTRYPOINT ["/app/nr-forest-client-legacy"]
