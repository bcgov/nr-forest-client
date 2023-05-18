# RedHat UBI 8 with nodejs 14
FROM registry.access.redhat.com/ubi8/ubi:8.7-1112@sha256:e3311058176628ad7f0f288f894ed2afef61be77ad01d53d5b69bca0f6b6cec1 as builder
RUN dnf module install -y nodejs:18

# Install packages, build and keep only prod packages
WORKDIR /app
COPY . ./
RUN npm ci --omit=dev && npm run build

# Deployment container
FROM caddy:2.6.4-alpine@sha256:5acae6b87b26a591d6d14ec2704b7f2cd94b888ad62af16a02356d3124198ede
EXPOSE 3000
COPY --from=builder /app/Caddyfile /etc/caddy/Caddyfile
COPY --from=builder /app/dist /srv

USER 1001
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:3000/