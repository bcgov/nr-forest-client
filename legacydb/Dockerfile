FROM gvenzl/oracle-free:23.3-slim-faststart

USER root
RUN usermod -u 1011270000 oracle
USER oracle

HEALTHCHECK --interval=30s --timeout=5s --start-period=5s --retries=3 CMD ["sh", "-c", "${ORACLE_BASE}/healthcheck.sh >/dev/null || exit 1"]

ENTRYPOINT ["container-entrypoint.sh"]