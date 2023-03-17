FROM postgres:15@sha256:50a96a21f2992518c2cb4601467cf27c7ac852542d8913c1872fe45cd6449947

HEALTHCHECK --interval=35s --timeout=4s CMD pg_isready -d db_prod
 
# Non-privileged user
USER postgres
