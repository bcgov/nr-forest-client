# syntax=docker/dockerfile:1
### Builder
FROM ghcr.io/graalvm/native-image-community:21.0.2 AS build

# --- SSL Root CA Import (Sectigo Public Server Authentication Root R46) ---
# Canada Post rotated its certificate chain to a root (Sectigo Public Server Authentication Root R46)
# missing from the older JDK bundled with this GraalVM image. We need that root in the trust store
# BEFORE native-image build so the embedded trust store trusts Canada Post endpoints.
# Source of truth: the reviewed, checked-in public certificate below (see backend/certs/ for
# provenance and rotation notes). This is public CA material, not a secret. The fingerprint is
# pinned and verified before import; there is no network fetch (e.g. crt.sh) in this build path.
# Not ARGs: these are fixed inside the RUN so `--build-arg` cannot override the pinned value.
# Bind-mounted (not COPY'd) so the PEM is never written into any image layer or build cache.

RUN --mount=type=bind,source=certs/sectigo-r46-root.pem,target=/tmp/sectigo-r46-root.pem,ro \
  set -eu; \
  CERT_FILE=/tmp/sectigo-r46-root.pem; \
  ALIAS="sectigo-r46-root"; \
  SECTIGO_R46_SHA256="7B:B6:47:A6:2A:EE:AC:88:BF:25:7A:A5:22:D0:1F:FE:A3:95:E0:AB:45:C7:3F:93:F6:56:54:EC:38:F2:5A:06"; \
  KEYSTORE="$JAVA_HOME/lib/security/cacerts"; \
  STOREPASS=changeit; \
  echo "--- Sectigo R46 CA import ---"; \
  [ -s "$CERT_FILE" ] || { echo "ERROR: $CERT_FILE is missing or empty. Ensure backend/certs/sectigo-r46-root.pem is committed and included in the build context."; exit 1; }; \
  grep -q "BEGIN CERTIFICATE" "$CERT_FILE" || { echo "ERROR: $CERT_FILE does not contain PEM certificate data."; exit 1; }; \
  [ -f "$KEYSTORE" ] || { echo "ERROR: keystore $KEYSTORE was not found in this image."; exit 1; }; \
  [ -w "$KEYSTORE" ] || { echo "ERROR: keystore $KEYSTORE is not writable."; exit 1; }; \
  echo "Validating certificate with keytool..."; \
  keytool -printcert -file "$CERT_FILE" > /tmp/sectigo-r46-root.info 2>/tmp/sectigo-r46-root.err \
    || { echo "ERROR: certificate is malformed and failed to parse:"; cat /tmp/sectigo-r46-root.err; rm -f /tmp/sectigo-r46-root.info /tmp/sectigo-r46-root.err; exit 1; }; \
  ACTUAL_SHA256=$(grep -i "SHA256:" /tmp/sectigo-r46-root.info | head -1 | sed 's/.*SHA256: *//' | tr -d ' \t'); \
  [ -n "$ACTUAL_SHA256" ] || { echo "ERROR: could not extract a SHA-256 fingerprint from the certificate."; exit 1; }; \
  EXPECTED_NORM=$(echo "$SECTIGO_R46_SHA256" | tr '[:lower:]' '[:upper:]' | tr -d ':'); \
  ACTUAL_NORM=$(echo "$ACTUAL_SHA256" | tr '[:lower:]' '[:upper:]' | tr -d ':'); \
  if [ "$ACTUAL_NORM" != "$EXPECTED_NORM" ]; then \
    echo "ERROR: certificate fingerprint mismatch."; \
    echo "  expected sha256: $SECTIGO_R46_SHA256"; \
    echo "  actual sha256:   $ACTUAL_SHA256"; \
    rm -f /tmp/sectigo-r46-root.info /tmp/sectigo-r46-root.err; \
    exit 1; \
  fi; \
  echo "Fingerprint verified (sha256=$ACTUAL_SHA256)."; \
  if keytool -list -keystore "$KEYSTORE" -storepass "$STOREPASS" -alias "$ALIAS" >/dev/null 2>&1; then \
    EXISTING_SHA256=$(keytool -list -v -keystore "$KEYSTORE" -storepass "$STOREPASS" -alias "$ALIAS" 2>/dev/null \
      | grep -i "SHA256:" | head -1 | sed 's/.*SHA256: *//' | tr -d ' \t' | tr '[:lower:]' '[:upper:]' | tr -d ':'); \
    if [ "$EXISTING_SHA256" = "$EXPECTED_NORM" ]; then \
      echo "Alias $ALIAS already present in $KEYSTORE with matching fingerprint; skipping import."; \
    else \
      echo "ERROR: alias $ALIAS already exists in $KEYSTORE with a mismatched fingerprint (found $EXISTING_SHA256)."; \
      rm -f /tmp/sectigo-r46-root.info /tmp/sectigo-r46-root.err; \
      exit 1; \
    fi; \
  else \
    echo "Importing certificate as alias $ALIAS..."; \
    keytool -importcert \
      -trustcacerts \
      -noprompt \
      -alias "$ALIAS" \
      -file "$CERT_FILE" \
      -keystore "$KEYSTORE" \
      -storepass "$STOREPASS"; \
  fi; \
  echo "Verifying successful installation..."; \
  keytool -list -keystore "$KEYSTORE" -storepass "$STOREPASS" -alias "$ALIAS" >/dev/null 2>&1 \
    || { echo "ERROR: post-import verification failed for alias $ALIAS."; exit 1; }; \
  \
  rm -f /tmp/sectigo-r46-root.info /tmp/sectigo-r46-root.err; \
  echo "--- Sectigo R46 CA import complete (alias=$ALIAS, sha256=$ACTUAL_SHA256) ---"

# Copy
WORKDIR /app
COPY pom.xml mvnw ./
COPY src ./src
COPY .mvn/ ./.mvn

# Build
RUN ./mvnw -Pnative package -DskipTests -Dskip.unit.tests=true

### Deployer
FROM gcr.io/distroless/java21-debian12:nonroot AS deploy

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
