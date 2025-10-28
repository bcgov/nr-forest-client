# Build static files
FROM node:alpine3.16 AS build

WORKDIR /app
COPY . .
RUN npm ci --ignore-scripts && npm install @esbuild/linux-x64 --no-save --ignore-scripts && npm run build

# Caddy
FROM caddy:2.10.2-alpine

# Copy static files and config
COPY --from=build /app/dist /srv
COPY Caddyfile /etc/caddy/Caddyfile
COPY robots.txt /srv/robots.txt
COPY setenv.sh /app/setenv.sh

# Packages and caddy format
RUN apk add --no-cache ca-certificates coreutils && \
    caddy fmt --overwrite /etc/caddy/Caddyfile && \
    mkdir -p /config/caddy /data/caddy && \
    chown -R 1001:1001 /config/caddy /data/caddy

# Port, health check and non-root user
EXPOSE 3000
HEALTHCHECK --interval=30s --timeout=3s CMD ["curl", "-f", "http://localhost:3000/"]

USER 1001

CMD ["/bin/sh", "-c", ". /app/setenv.sh; caddy run --config /etc/caddy/Caddyfile --adapter caddyfile"]
