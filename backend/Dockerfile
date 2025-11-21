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
        find /etc/ssl/certs \( -name "*.pem" -o -name "*.crt" \) 2>/dev/null | while read -r cert; do \
            ALIAS=$(echo "$cert" | sed 's/[^a-zA-Z0-9]/_/g')_$(sha256sum "$cert" 2>/dev/null | cut -d' ' -f1 | head -c 8 || echo "$(basename "$cert" | sed 's/\.[^.]*$//')"); \
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
        echo "Verifying truststore update..."; \
        TRUSTSTORE_COUNT=$(keytool -list -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit 2>/dev/null | grep -c "Alias name:" || echo "0"); \
        echo "Total certificates in truststore: $TRUSTSTORE_COUNT"; \
        echo "Certificate update complete"; \
    else \
        echo "Warning: Java truststore or keytool not found, skipping certificate import"; \
    fi

# Download and add Sectigo root CA certificate for Canada Post
# Canada Post's certificate chain requires Sectigo Public Server Authentication Root R46
# Reference: https://www.sectigo.com/knowledge-base/detail/Sectigo-Root-Certificates/kA03l000000c4KV
RUN if [ -f "$JAVA_HOME/lib/security/cacerts" ] && command -v keytool > /dev/null; then \
        echo "Installing curl and openssl for certificate download..."; \
        microdnf install -y curl openssl && microdnf clean all; \
        mkdir -p /tmp/certs; \
        echo "Downloading Sectigo Public Server Authentication Root R46..."; \
        # Try multiple download methods from crt.sh for reliability \
        CERT_DOWNLOADED=0; \
        if curl -sL "https://crt.sh/?d=4256644734" 2>/dev/null | grep -A 50 "Sectigo Public Server Authentication Root R46" | sed -n '/-----BEGIN CERTIFICATE-----/,/-----END CERTIFICATE-----/p' > /tmp/certs/sectigo-root.crt 2>/dev/null && [ -s /tmp/certs/sectigo-root.crt ]; then \
            CERT_DOWNLOADED=1; \
            echo "Certificate downloaded using crt.sh method 1"; \
        elif curl -sL "https://crt.sh/?id=4256644734" 2>/dev/null | sed -n '/-----BEGIN CERTIFICATE-----/,/-----END CERTIFICATE-----/p' > /tmp/certs/sectigo-root.crt 2>/dev/null && [ -s /tmp/certs/sectigo-root.crt ]; then \
            CERT_DOWNLOADED=1; \
            echo "Certificate downloaded using crt.sh method 2"; \
        fi; \
        if [ "$CERT_DOWNLOADED" -eq 1 ] && [ -f /tmp/certs/sectigo-root.crt ] && [ -s /tmp/certs/sectigo-root.crt ]; then \
            # Validate certificate format and subject \
            if openssl x509 -in /tmp/certs/sectigo-root.crt -noout -subject 2>/dev/null | grep -q "Sectigo Public Server Authentication Root R46"; then \
                ACTUAL_FINGERPRINT=$(openssl x509 -in /tmp/certs/sectigo-root.crt -noout -fingerprint -sha256 2>/dev/null | cut -d'=' -f2); \
                echo "Certificate subject verified. SHA-256 fingerprint: $ACTUAL_FINGERPRINT"; \
                echo "Importing Sectigo Public Server Authentication Root R46..."; \
                IMPORT_OUTPUT=$(keytool -import -noprompt -trustcacerts -file /tmp/certs/sectigo-root.crt \
                    -alias "Sectigo_Public_Server_Authentication_Root_R46" \
                    -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit 2>&1); \
                if echo "$IMPORT_OUTPUT" | grep -q "already exists"; then \
                    echo "Info: Sectigo root CA already exists in truststore"; \
                else \
                    echo "$IMPORT_OUTPUT"; \
                fi; \
                echo "Verifying Sectigo root CA was imported..."; \
                if keytool -list -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit 2>/dev/null | grep -qiE "sectigo|Sectigo_Public_Server_Authentication_Root_R46"; then \
                    echo "Success: Sectigo root CA verified in truststore"; \
                else \
                    echo "Warning: Sectigo root CA may not have been imported correctly" >&2; \
                fi; \
            else \
                echo "ERROR: Downloaded certificate subject verification failed!" >&2; \
                openssl x509 -in /tmp/certs/sectigo-root.crt -noout -subject 2>&1 || echo "Certificate file is invalid" >&2; \
                rm -f /tmp/certs/sectigo-root.crt; \
            fi; \
        else \
            echo "ERROR: Could not download Sectigo root CA certificate from crt.sh!" >&2; \
            echo "This may cause SSL handshake failures with Canada Post API" >&2; \
        fi; \
        rm -rf /tmp/certs; \
    else \
        echo "Warning: Required tools not found, skipping Sectigo certificate download"; \
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
