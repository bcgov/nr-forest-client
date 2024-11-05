FROM postgres:13

ARG BUILD_DATE

LABEL org.opencontainers.image.created="$BUILD_DATE"
LABEL org.opencontainers.image.authors="Natural Resource Information and Digital Services"
LABEL org.opencontainers.image.url="https://github.com/bcgov/nr-forest-client%2Fdatabase"
LABEL org.opencontainers.image.documentation="https://github.com/bcgov/nr-forest-client/wiki"
LABEL org.opencontainers.image.source="https://github.com/bcgov/nr-forest-client"
LABEL org.opencontainers.image.version="postgres:13"
LABEL org.opencontainers.image.licenses="Apache-2.0"
LABEL org.opencontainers.image.title="Postgres Database with pgcrypto extension"
LABEL org.opencontainers.image.vendor="Government of British Columbia"
LABEL org.opencontainers.image.description="Postgres Database with pgcrypto extension enabled"
LABEL org.opencontainers.image.base.name="postgres:13"

HEALTHCHECK --interval=35s --timeout=4s CMD pg_isready -d db_prod

# Non-privileged user
USER postgres
