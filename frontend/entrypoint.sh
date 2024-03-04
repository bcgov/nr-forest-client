#!/bin/sh

# Replace the placeholder in index.html with the actual NONCE value.
sed -i "s/{NONCE}/$RANDOM_NONCE/g" /srv/index.html

# Execute the original entrypoint command of Caddy.
exec /usr/bin/caddy run --config /etc/caddy/Caddyfile --adapter caddyfile
