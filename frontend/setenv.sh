export ENTRY_CHKSM=$(sha256sum /srv/assets/*.js | awk '{ print $1; exit }')
export DATA_CHKSM=$(sha256sum /srv/data/config.js | awk '{ print $1 }')