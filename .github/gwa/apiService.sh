export NS="d2723f"
export NAME="nrfc"
export ZONE="test"
export SERVICE="nrfc-test-backend"

echo "
services:
- name: $NAME
  host: $SERVICE.$NS-$ZONE.svc
  tags: [ ns.$NS.$ZONE ]
  port: 80
  protocol: http
  retries: 0
  routes:
  - name: $NAME-route
    tags: [ ns.$NS.$ZONE ]
    hosts:
    - $NAME.api.gov.bc.ca
    paths:
    - /
    methods:
    - GET
    strip_path: false
    https_redirect_status_code: 426
    path_handling: v0
" > service.yaml