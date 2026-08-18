#!/bin/sh
set -eu

environment="${APP_ENV:-development}"
api_base_url="${API_BASE_URL:-}"

if [ -z "$api_base_url" ]; then
  case "$environment" in
    development|dev|local|test)
      exit 0
      ;;
    *)
      echo "API_BASE_URL e obrigatoria no ambiente $environment." >&2
      exit 1
      ;;
  esac
fi

while [ "${api_base_url%/}" != "$api_base_url" ]; do
  api_base_url="${api_base_url%/}"
done

if ! printf '%s\n' "$api_base_url" \
  | grep -Eq '^https?://(\[[0-9A-Fa-f:]+\]|[A-Za-z0-9.-]+)(:[0-9]+)?(/[A-Za-z0-9._~!$&()*+,;=:@%/-]*)?$'; then
  echo "API_BASE_URL invalida. Use uma URL http:// ou https:// sem espacos, aspas, query ou fragmento." >&2
  exit 1
fi

API_BASE_URL="$api_base_url"
export API_BASE_URL

envsubst '${API_BASE_URL}' \
  < /opt/sandra-x-andreia/config.template.js \
  > /usr/share/nginx/html/config.js
