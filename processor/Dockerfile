### Builder
FROM eclipse-temurin:21-jdk-jammy AS build

# RELAX SECURITY: enable legacy TLS versions/algorithms (e.g. TLS 1.0/1.1, SHA1, DH/RSA keys < 2048)
# so the application can complete a handshake with the legacy Oracle DB's old cert.
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
RUN ./mvnw clean package -DskipTests -Dtests.skip=true -Dskip.unit.tests=true

### Deployer
FROM eclipse-temurin:21-jre-jammy AS deploy
ARG PORT=3100

# Copy
WORKDIR /app
COPY --from=build /app/target/nr-forest-client-processor.jar ./nr-forest-client-processor.jar

# User, port and health check
USER 1001
EXPOSE ${PORT}
HEALTHCHECK CMD curl -f http://localhost:${PORT}/actuator/health | grep '"status":"UP"'

# Startup
ENTRYPOINT ["java", "-jar", "/app/nr-forest-client-processor.jar","--spring.profiles.active=container"]
