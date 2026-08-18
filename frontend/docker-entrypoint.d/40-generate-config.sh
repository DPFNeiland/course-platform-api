#!/bin/sh
set -eu

if [ -z "${API_BASE_URL:-}" ]; then
  exit 0
fi

envsubst '${API_BASE_URL}' \
  < /opt/sandra-x-andreia/config.template.js \
  > /usr/share/nginx/html/config.js
