# RedHat UBI 8 with nodejs 14
FROM registry.access.redhat.com/ubi8/ubi:8.7-1112@sha256:e3311058176628ad7f0f288f894ed2afef61be77ad01d53d5b69bca0f6b6cec1 as builder
RUN dnf module install -y nodejs:18

# Install packages, build and keep only prod packages
WORKDIR /app
COPY . ./
RUN npm ci --omit=dev && npm run build

# Deployment container
FROM caddy:2.6.4-alpine@sha256:eefd3d61e9ee8f35e046f614982d9a970006e3943c6e5f09957a4048f4c80d35
EXPOSE 3000
COPY --from=builder /app/Caddyfile /etc/caddy/Caddyfile
COPY --from=builder /app/dist /srv

RUN chmod +x /usr/bin/caddy
USER app
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:3000/