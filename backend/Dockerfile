### Builder
FROM ghcr.io/graalvm/native-image:22.3.3 AS build

# Update CA certificates in the build image to ensure latest certs are bundled
# For GraalVM native images, Spring Boot Native automatically bundles system certificates
# during compilation, so updating them here ensures the native binary includes the latest certs.
# The native binary embeds these certificates, so no runtime cert files are needed.
RUN microdnf install -y --refresh ca-certificates && \
    update-ca-certificates 2>/dev/null || true && \
    microdnf clean all

# Ensure Java truststore is updated with system CA certificates for GraalVM certificate bundling
# Spring Boot Native reads from both /etc/ssl/certs and Java's cacerts truststore
RUN if [ -f "$JAVA_HOME/lib/security/cacerts" ] && command -v keytool > /dev/null; then \
        find /etc/ssl/certs -name "*.pem" -o -name "*.crt" 2>/dev/null | while read cert; do \
            keytool -import -noprompt -trustcacerts -file "$cert" -alias "$(basename $cert .pem | basename $cert .crt)" \
                -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit 2>/dev/null || true; \
        done; \
        keytool -list -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit | head -5 || true; \
    fi

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
