FROM postgres:13

# Enable the pgcrypto extension
RUN echo "CREATE EXTENSION IF NOT EXISTS pgcrypto;" >> /docker-entrypoint-initdb.d/init.sql

HEALTHCHECK --interval=35s --timeout=4s CMD pg_isready -d db_prod

# Non-privileged user
USER postgres
