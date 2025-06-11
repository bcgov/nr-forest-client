FROM postgres:13@sha256:467e7f2fb97b2f29d616e0be1d02218a7bbdfb94eb3cda7461fd80165edfd1f7

HEALTHCHECK --interval=35s --timeout=4s CMD pg_isready -d db_prod
 
# Non-privileged user
USER postgres
