### Builder
FROM ghcr.io/graalvm/native-image:22.3.3 AS build

# --- SSL Root CA Refresh (Sectigo R46) ---
# Canada Post rotated its certificate chain to a root (Sectigo Public Server Authentication Root R46)
# missing from the older JDK bundled with this GraalVM image. We need that root in the trust store
# BEFORE native-image build so the embedded trust store trusts Canada Post endpoints.
# Policy: do NOT commit the PEM. Approaches supported:
#  1. Provide the PEM via build arg SECTIGO_R46_PEM (recommended; store content in GitHub/OpenShift secret).
#  2. Fallback: attempt network fetch from crt.sh (public CT log) if build arg not supplied.
# If neither succeeds, build continues (native image created) but import step will fail -> non-zero exit to surface issue.
ARG SECTIGO_R46_PEM=""
RUN set -e; \
  CERT_FILE=/tmp/sectigo-r46-root.pem; \
  if [ -n "$SECTIGO_R46_PEM" ]; then \
  printf '%s\n' "$SECTIGO_R46_PEM" > "$CERT_FILE"; \
  else \
  echo "No build arg provided; attempting remote fetch of Sectigo R46 root"; \
  curl -fsSL 'https://crt.sh/?d=4256644734' -o "$CERT_FILE" || { echo "Remote fetch failed; please supply SECTIGO_R46_PEM."; exit 1; }; \
  fi; \
  keytool -importcert -trustcacerts -noprompt -alias sectigo-r46-root -file "$CERT_FILE" \
  -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit; \
  keytool -list -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit | grep -i sectigo || echo "Imported but grep did not match (verify manually)"; \
  rm -f "$CERT_FILE"

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
