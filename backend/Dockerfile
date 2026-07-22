### Builder
FROM ghcr.io/graalvm/native-image-community:25 AS build

# RELAX SECURITY: enable legacy TLS versions/algorithms (e.g. TLS 1.0/1.1, SHA1, DH/RSA keys < 2048)
# so the native image can complete a handshake with the legacy Oracle DB's old cert.
# GraalVM native-image bakes the JDK security providers into the binary at build time, so this
# must be patched here (pre-compile) rather than via a runtime java.security override.
RUN sed -i '/^jdk.tls.disabledAlgorithms=/,/[^\\]$/c\jdk.tls.disabledAlgorithms=SSLv3' "$JAVA_HOME/conf/security/java.security"

# --- SSL Root CA Refresh (Sectigo R46) ---
# Canada Post rotated its certificate chain to a root (Sectigo Public Server Authentication Root R46)
# missing from the older JDK bundled with this GraalVM image. We need that root in the trust store
# BEFORE native-image build so the embedded trust store trusts Canada Post endpoints.
# Policy: do NOT commit the PEM. Approaches supported:
#  1. Provide the PEM via build arg SECTIGO_R46_PEM (recommended; store content in GitHub/OpenShift secret).
#  2. Fallback: attempt network fetch from crt.sh (public CT log) if build arg not supplied.
ARG SECTIGO_R46_PEM=""

RUN set -e; \
  CERT_FILE=/tmp/sectigo-r46-root.pem; \
  echo "--- Starting Sectigo CA Import ---"; \
  if [ -n "$SECTIGO_R46_PEM" ]; then \
    echo "Using provided SECTIGO_R46_PEM build argument."; \
    printf '%b' "$SECTIGO_R46_PEM" > "$CERT_FILE"; \
  else \
    echo "Attempting remote fetch of Sectigo R46 root..."; \
    curl -A "Mozilla/5.0 (Windows NT 10.0; Win64; x64)" \
         --retry 5 \
         --retry-delay 5 \
         --retry-connrefused \
         -fsSL 'https://crt.sh/?d=4256644734' \
         -o "$CERT_FILE" \
      || { echo "ERROR: Curl failed to connect to crt.sh."; exit 1; }; \
    \
    echo "Verifying downloaded file format..."; \
    grep -q "BEGIN CERTIFICATE" "$CERT_FILE" || { \
      echo "ERROR: Downloaded file is not a valid PEM. crt.sh likely returned an HTML error page. File content:"; \
      cat "$CERT_FILE"; \
      exit 1; \
    }; \
  fi; \
  \
  echo "Checking if alias already exists in trust store..."; \
  if keytool -list -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit -alias sectigo-r46-root >/dev/null 2>&1; then \
    echo "Alias sectigo-r46-root already exists, skipping import."; \
  else \
    echo "Importing certificate..."; \
    keytool -importcert \
      -trustcacerts \
      -noprompt \
      -alias sectigo-r46-root \
      -file "$CERT_FILE" \
      -keystore "$JAVA_HOME/lib/security/cacerts" \
      -storepass changeit; \
  fi; \
  \
  echo "Verifying successful installation..."; \
  keytool -list \
    -keystore "$JAVA_HOME/lib/security/cacerts" \
    -storepass changeit \
    -alias sectigo-r46-root >/dev/null; \
  \
  rm -f "$CERT_FILE"; \
  echo "--- Sectigo CA Import Successful ---"

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
COPY --from=build /app/target/nr-forest-client-backend ./nr-forest-client-backend

# User, port and health check
USER 1001
EXPOSE 8080
HEALTHCHECK CMD curl -f http://localhost:8080/actuator/health | grep '"status":"UP"'

# Startup
ENTRYPOINT ["/app/nr-forest-client-backend","--spring.profiles.active=container"]
