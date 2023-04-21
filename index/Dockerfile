FROM docker.elastic.co/elasticsearch/elasticsearch-oss:7.12.0

# Switch to the Elasticsearch user
USER elasticsearch

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=5 CMD curl -f http://localhost:9200/_cluster/health || exit 1

# Expose Elasticsearch ports
EXPOSE 9200 9300