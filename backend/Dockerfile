### Builder
FROM ghcr.io/graalvm/native-image:22.3.3 AS build

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