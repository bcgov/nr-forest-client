FROM postgres:13

HEALTHCHECK --interval=35s --timeout=4s CMD pg_isready -d db_prod
 
# Non-privileged user
USER postgres
