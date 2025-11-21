### Builder
FROM ghcr.io/graalvm/native-image:22.3.3 AS build

# Update CA certificates in the build image to ensure latest certs are bundled
# For GraalVM native images, Spring Boot Native automatically bundles system certificates
# during compilation, so updating them here ensures the native binary includes the latest certs.
# The native binary embeds these certificates, so no runtime cert files are needed.
RUN if command -v microdnf > /dev/null; then \
        microdnf install -y --refresh ca-certificates && \
        update-ca-certificates 2>/dev/null || true && \
        microdnf clean all; \
    elif command -v yum > /dev/null; then \
        yum install -y ca-certificates && \
        update-ca-certificates 2>/dev/null || true && \
        yum clean all; \
    elif command -v apt-get > /dev/null; then \
        apt-get update && apt-get install -y --no-install-recommends ca-certificates && \
        update-ca-certificates && \
        apt-get clean && rm -rf /var/lib/apt/lists/*; \
    fi && \
    # Ensure Java truststore is updated if Java is available (for GraalVM certificate bundling)
    if [ -f "$JAVA_HOME/lib/security/cacerts" ] && command -v keytool > /dev/null; then \
        find /etc/ssl/certs -name "*.pem" -o -name "*.crt" | while read cert; do \
            keytool -import -noprompt -trustcacerts -file "$cert" -alias "$(basename $cert)" \
                -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit 2>/dev/null || true; \
        done; \
    fi || true

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
COPY --from=build /app/target/nr-forest-client-backend ./nr-forest-client-backend

# User, port and health check
USER 1001
EXPOSE 8080
HEALTHCHECK CMD curl -f http://localhost:8080/actuator/health | grep '"status":"UP"'

ENV SPRING_PROFILES_ACTIVE=container

# Startup
ENTRYPOINT ["/app/nr-forest-client-backend"]
