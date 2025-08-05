FROM gvenzl/oracle-free:23.9-slim-faststart

# Copy the custom scripts we need to the image
COPY --chown=oracle:oracle removeDatabase ${ORACLE_BASE}/removeDatabase

# Give permission to the scripts
RUN chmod +x ${ORACLE_BASE}/removeDatabase

USER root
RUN usermod -u 1011270000 oracle
USER oracle

HEALTHCHECK --interval=30s --timeout=5s --start-period=5s --retries=3 CMD ["sh", "-c", "${ORACLE_BASE}/healthcheck.sh >/dev/null || exit 1"]

ENTRYPOINT ["container-entrypoint.sh"]
