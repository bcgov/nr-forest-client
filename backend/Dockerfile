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
# Spring Boot Native automatically bundles certificates from /etc/ssl/certs and Java's cacerts truststore
# We update Java's truststore so Spring Boot Native can detect and bundle them during native compilation
RUN if [ -f "$JAVA_HOME/lib/security/cacerts" ] && command -v keytool > /dev/null; then \
        echo "Updating Java truststore with system CA certificates..."; \
        find /etc/ssl/certs -name "*.pem" -o -name "*.crt" 2>/dev/null | while read -r cert; do \
            ALIAS=$(basename "$cert" | sed 's/\.[^.]*$//'); \
            ERR_OUTPUT=$(keytool -import -noprompt -trustcacerts -file "$cert" -alias "$ALIAS" \
                -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit 2>&1); \
            EXIT_CODE=$?; \
            if [ $EXIT_CODE -ne 0 ]; then \
                if echo "$ERR_OUTPUT" | grep -q "already exists"; then \
                    echo "Info: Certificate $ALIAS already exists in truststore, skipping." >&2; \
                else \
                    echo "Warning: Failed to import certificate $cert (alias: $ALIAS):" >&2; \
                    echo "$ERR_OUTPUT" >&2; \
                fi; \
            fi; \
        done; \
        echo "Verifying truststore contains certificates..."; \
        keytool -list -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit 2>/dev/null | head -10 || echo "Warning: Could not list truststore"; \
        echo "Checking for Sectigo CA certificates (Canada Post root CA)..."; \
        keytool -list -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit 2>/dev/null | \
            grep -iE "sectigo" || echo "WARNING: No Sectigo CA certificates found in truststore!"; \
        echo "Checking for Entrust CA certificates (Canada Post intermediate CA)..."; \
        keytool -list -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit 2>/dev/null | \
            grep -iE "entrust" || echo "WARNING: No Entrust CA certificates found in truststore!"; \
        echo "Checking system certificate store for Sectigo certificates..."; \
        find /etc/ssl/certs -iname "*sectigo*" 2>/dev/null | head -5 || echo "No Sectigo certificates found in /etc/ssl/certs"; \
        echo "Checking system certificate store for Entrust certificates..."; \
        find /etc/ssl/certs -iname "*entrust*" 2>/dev/null | head -5 || echo "No Entrust certificates found in /etc/ssl/certs"; \
        echo "Total certificates in truststore:"; \
        keytool -list -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit 2>/dev/null | grep -c "Alias name:" || echo "0"; \
        echo "Certificate update complete"; \
    else \
        echo "Warning: Java truststore or keytool not found, skipping certificate import"; \
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
