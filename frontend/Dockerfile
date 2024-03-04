# Build static files
FROM node:alpine3.16 AS build

ARG APP_VERSION=0.0.1

WORKDIR /app
COPY . .
RUN npm ci && \
    npm version ${APP_VERSION} --no-git-tag-version && \
    npm run build

# Caddy
FROM caddy:2.7.6-alpine

# Copy static files and config
COPY --from=build /app/dist /srv
COPY Caddyfile /etc/caddy/Caddyfile
COPY entrypoint.sh /entrypoint.sh

# Packages and caddy format
RUN apk add --no-cache ca-certificates && \
    caddy fmt --overwrite /etc/caddy/Caddyfile && \
    chmod +x /entrypoint.sh

# Port, health check and non-root user
EXPOSE 3000
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:3000/

USER root
RUN chown caddy:caddy /srv/index.html && \
    chmod 644 /srv/index.html

# Use the entrypoint script
USER 1001
ENTRYPOINT ["/entrypoint.sh"]
