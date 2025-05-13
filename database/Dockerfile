FROM postgres:13@sha256:7f122a2d2c1f2ba92fda0dbd1020bb47ef5b163c1e537e903b7a525e24c1ba44

HEALTHCHECK --interval=35s --timeout=4s CMD pg_isready -d db_prod
 
# Non-privileged user
USER postgres
