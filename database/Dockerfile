FROM postgres:13@sha256:9b6a9504a4b804c8f0cf52f4d16477289f8e1dfa269a689d778feaca6fbe1f4f

HEALTHCHECK --interval=35s --timeout=4s CMD pg_isready -d db_prod
 
# Non-privileged user
USER postgres
