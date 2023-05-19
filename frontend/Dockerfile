# RedHat UBI 8 with nodejs 14
FROM registry.access.redhat.com/ubi8/ubi:8.8-854@sha256:a7143118671dfc61aca46e8ab9e488500495a3c4c73a69577ca9386564614c13 as builder
RUN dnf module install -y nodejs:18

# Install packages, build and keep only prod packages
WORKDIR /app
COPY . ./
RUN npm ci --omit=dev && npm run build

# Deployment container
FROM caddy:2.4.6-alpine@sha256:c8555e2ca66576a4404c2b5f31d843a5378dea6134acb500502901b563efebcf
EXPOSE 3000
COPY --from=builder /app/Caddyfile /etc/caddy/Caddyfile
COPY --from=builder /app/dist /srv

USER 1001
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:3000/