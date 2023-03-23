# RedHat UBI 8 with nodejs 14
FROM registry.access.redhat.com/ubi8/ubi:8.7-1090@sha256:70fc843d4eb70159799e065d2830726b884c93950f8891e263d6328af6141173 as builder
RUN dnf module install -y nodejs:18

# Install packages, build and keep only prod packages
WORKDIR /app
COPY . ./
RUN npm ci --omit=dev && npm run build

# Deployment container
FROM caddy:2.4.6-alpine
EXPOSE 3000
COPY --from=builder /app/Caddyfile /etc/caddy/Caddyfile
COPY --from=builder /app/dist /srv

USER 1001
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost/:3000