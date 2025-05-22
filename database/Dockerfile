FROM postgres:13@sha256:d70d466e2a7daf32b35eb9c9ddd1a0079881a00349f2d54ae73774b01e0b1655

HEALTHCHECK --interval=35s --timeout=4s CMD pg_isready -d db_prod
 
# Non-privileged user
USER postgres
