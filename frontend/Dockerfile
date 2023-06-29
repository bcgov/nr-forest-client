# Build static files
FROM node:alpine3.16 AS build

WORKDIR /app
COPY . .
RUN npm ci --omit=dev && \
    npm run build

# Caddy
FROM caddy:2.6.4-alpine

# Copy static files and config
COPY --from=build /app/dist /srv
COPY Caddyfile /etc/caddy/Caddyfile

# Packages and caddy format
RUN apk add --no-cache ca-certificates && \
    caddy fmt --overwrite /etc/caddy/Caddyfile

# Port, health check and non-root user
EXPOSE 3000
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:3000/
USER 1001
