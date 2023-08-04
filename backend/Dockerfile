### Builder
FROM ghcr.io/graalvm/native-image-community:20-ol7 AS build

# Receiving app version
ARG APP_VERSION=0.0.1

# Copy
WORKDIR /app
COPY pom.xml mvnw ./
COPY src ./src
COPY .mvn/ ./.mvn

# Setting app version
RUN ./mvnw versions:set -DnewVersion=${APP_VERSION} -f pom.xml -DskipTests -Dtests.skip=true -Dskip.unit.tests=true && \
    ./mvnw versions:commit -f pom.xml -DskipTests -Dtests.skip=true -Dskip.unit.tests=true

# Build
RUN ./mvnw -Pnative native:compile


### Deployer
FROM gcr.io/distroless/java-base:nonroot AS deploy
ARG PORT=8080

# Copy
WORKDIR /app
COPY --from=build /app/target/nr-forest-client-backend ./nr-forest-client-backend

# User, port and health check
USER 1001
EXPOSE ${PORT}
HEALTHCHECK CMD curl -f http://localhost:${PORT}/actuator/health | grep '"status":"UP"'

# Startup
ENTRYPOINT ["/app/nr-forest-client-backend"]