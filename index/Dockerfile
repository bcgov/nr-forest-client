FROM elasticsearch:8.7.0@sha256:861409a3479ce337308777570ea6caa179731c59e10181d2931a110766b0d2fb

# Switch to the Elasticsearch user
USER elasticsearch

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=5 CMD curl -f http://localhost:9200/_cluster/health || exit 1

# Expose Elasticsearch ports
EXPOSE 9200 9300