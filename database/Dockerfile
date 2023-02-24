FROM postgres:13@sha256:0876afe95f304506a42c6fcd5b3b272340c394389086dcd33fe4c1ac999ff118

HEALTHCHECK --interval=35s --timeout=4s CMD pg_isready -d db_prod
 
# Non-privileged user
USER postgres
