FROM postgres:13@sha256:e1195666dc3edf6c8447bea6df9d7bccfdda66ab927d1f68b1b6e0cc2262c232

HEALTHCHECK --interval=35s --timeout=4s CMD pg_isready -d db_prod
 
# Non-privileged user
USER postgres
