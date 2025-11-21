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

# Download and add Sectigo root CA certificate for Canada Post
# Canada Post's certificate chain requires Sectigo Public Server Authentication Root R46
# The intermediate certificate (Entrust OV TLS Issuing RSA CA 2) is provided by the server
# We need to download the root CA (Sectigo R46) separately since servers don't send root certificates
RUN if [ -f "$JAVA_HOME/lib/security/cacerts" ] && command -v keytool > /dev/null; then \
        echo "Installing curl and openssl for certificate operations..."; \
        microdnf install -y curl openssl unzip && microdnf clean all || true; \
        mkdir -p /tmp/certs; \
        echo "Extracting intermediate certificate from Canada Post server..."; \
        echo | openssl s_client -connect ws1.postescanada-canadapost.ca:443 -showcerts 2>/dev/null | \
            awk '/-----BEGIN CERTIFICATE-----/,/-----END CERTIFICATE-----/ {print}' > /tmp/certs/chain.pem || true; \
        if [ -f /tmp/certs/chain.pem ] && [ -s /tmp/certs/chain.pem ]; then \
            # Split certificates (each cert in chain)
            csplit -f /tmp/certs/cert- -s /tmp/certs/chain.pem '/-----BEGIN CERTIFICATE-----/' '{*}' 2>/dev/null || \
            awk '/-----BEGIN CERTIFICATE-----/ {i++; close(file); file="/tmp/certs/cert-" i ".pem"} {print > file}' /tmp/certs/chain.pem || true; \
            # Import intermediate certificate (Entrust OV TLS Issuing RSA CA 2)
            for cert in /tmp/certs/cert-*.pem; do \
                if [ -f "$cert" ] && [ -s "$cert" ]; then \
                    SUBJECT=$(openssl x509 -noout -subject -in "$cert" 2>/dev/null | sed 's/subject=//'); \
                    CN=$(echo "$SUBJECT" | sed -n 's/.*CN=\([^,]*\).*/\1/p' | head -1); \
                    if [ -n "$CN" ] && echo "$CN" | grep -q "Entrust"; then \
                        ALIAS=$(echo "$CN" | tr ' ' '_' | tr -d '/' | sed 's/[^a-zA-Z0-9_]//g' | cut -c1-64); \
                        echo "Importing intermediate certificate: $CN"; \
                        keytool -import -noprompt -trustcacerts -file "$cert" -alias "$ALIAS" \
                            -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit 2>&1 | \
                            grep -v "already exists" || true; \
                    fi; \
                fi; \
            done; \
        fi; \
        echo "Downloading Sectigo Public Server Authentication Root R46 certificate..."; \
        # Download from Sectigo's certificate repository
        curl -sL "https://support.sectigo.com/articles/Knowledge/Sectigo-Intermediate-Certificates" 2>/dev/null | \
            grep -o 'https://[^"]*root[^"]*\.crt' | head -1 | xargs curl -sL 2>/dev/null > /tmp/certs/sectigo-root.crt || \
        curl -sL "https://www.sectigo.com/uploads/files/SectigoRootCertificates.zip" 2>/dev/null | \
            unzip -p - 2>/dev/null | grep -B 5 -A 20 "R46" | \
            sed -n '/-----BEGIN CERTIFICATE-----/,/-----END CERTIFICATE-----/p' > /tmp/certs/sectigo-root.crt || true; \
        # If download failed, try to construct from certificate transparency log
        if [ ! -f /tmp/certs/sectigo-root.crt ] || [ ! -s /tmp/certs/sectigo-root.crt ]; then \
            echo "Attempting alternative method to get Sectigo root certificate..."; \
            # Use openssl to get certificate by getting the issuer from intermediate
            if [ -f /tmp/certs/chain.pem ]; then \
                # Extract issuer info and note that we need Sectigo Public Server Authentication Root R46
                echo "Note: Sectigo root certificate needs to be added manually if not found in bundle"; \
            fi; \
        else \
            echo "Importing Sectigo Public Server Authentication Root R46..."; \
            keytool -import -noprompt -trustcacerts -file /tmp/certs/sectigo-root.crt \
                -alias "Sectigo_Public_Server_Authentication_Root_R46" \
                -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit 2>&1 | \
                grep -v "already exists" || true; \
            echo "Verifying Sectigo root CA was imported..."; \
            keytool -list -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit 2>/dev/null | \
                grep -iE "sectigo|Sectigo_Public_Server_Authentication_Root_R46" || echo "Warning: Sectigo root CA may not have been imported"; \
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
