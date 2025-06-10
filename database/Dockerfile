FROM postgres:13@sha256:bd97d1d6f3142ebf8f165df09f47ee054b446b46e40004d7edef738952012dea

HEALTHCHECK --interval=35s --timeout=4s CMD pg_isready -d db_prod
 
# Non-privileged user
USER postgres
