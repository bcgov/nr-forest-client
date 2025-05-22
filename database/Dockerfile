FROM postgres:13@sha256:fe8bebcf0f8a57e6328757698374ef2955f6467779b6d71c3021a3770edd07c9

HEALTHCHECK --interval=35s --timeout=4s CMD pg_isready -d db_prod
 
# Non-privileged user
USER postgres
