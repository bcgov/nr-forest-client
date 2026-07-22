### Builder
FROM ghcr.io/graalvm/native-image-community:25 AS build

# RELAX SECURITY: enable legacy TLS versions/algorithms (e.g. TLS 1.0/1.1, SHA1, DH/RSA keys < 2048)
# so the native image can complete a handshake with the legacy Oracle DB's old cert.
# GraalVM native-image bakes the JDK security providers into the binary at build time, so this
# must be patched here (pre-compile) rather than via a runtime java.security override.
RUN sed -i '/^jdk.tls.disabledAlgorithms=/,/[^\\]$/c\jdk.tls.disabledAlgorithms=SSLv3' "$JAVA_HOME/conf/security/java.security"

# Copy
WORKDIR /app
COPY pom.xml ./
COPY src ./src
COPY mvnw ./
COPY .mvn/wrapper/maven-wrapper.properties ./.mvn/wrapper/maven-wrapper.properties

# Ensure mvnw has execution permissions
RUN chmod +x mvnw

# Build
RUN ./mvnw -Pnative native:compile -DskipTests -Dskip.unit.tests=true

### Deployer
FROM gcr.io/distroless/base:nonroot AS deploy

# Copy
WORKDIR /app
COPY --from=build /app/target/nr-forest-client-legacy ./nr-forest-client-legacy

# User, port and health check
USER 1001
EXPOSE 9000
HEALTHCHECK CMD curl -f http://localhost:9000/actuator/health | grep '"status":"UP"'

# Startup
ENTRYPOINT ["/app/nr-forest-client-legacy","--spring.profiles.active=container"]
