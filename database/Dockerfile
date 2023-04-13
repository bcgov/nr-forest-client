FROM postgres:13@sha256:00f455399f30cc3f2fe4185476601438b7a4959c74653665582d7c313a783d51

HEALTHCHECK --interval=35s --timeout=4s CMD pg_isready -d db_prod
 
# Non-privileged user
USER postgres
