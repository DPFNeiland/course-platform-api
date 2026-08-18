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

host_label='[A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?'
host="(\\[[0-9A-Fa-f:]+\\]|${host_label}(\\.${host_label})*)"
path='(/[A-Za-z0-9._~:@%+/-]*)?'
url_pattern="^https?://${host}(:[0-9]{1,5})?${path}$"

if ! printf '%s\n' "$api_base_url" \
  | grep -Eq "$url_pattern"; then
  echo "API_BASE_URL invalida. Use uma URL http:// ou https:// sem espacos, aspas, query ou fragmento." >&2
  exit 1
fi

authority="${api_base_url#*://}"
authority="${authority%%/*}"
port=''
case "$authority" in
  \[*\]:*) port="${authority##*:}" ;;
  \[*\]) ;;
  *:*) port="${authority##*:}" ;;
esac

if [ -n "$port" ] && { [ "$port" -eq 0 ] || [ "$port" -gt 65535 ]; }; then
  echo "API_BASE_URL invalida. A porta deve estar entre 1 e 65535." >&2
  exit 1
fi

case "$environment" in
  staging|production|prod)
    case "$api_base_url" in
      https://*) ;;
      *)
        echo "API_BASE_URL deve utilizar HTTPS no ambiente $environment." >&2
        exit 1
        ;;
    esac
    ;;
esac

API_BASE_URL="$api_base_url"
export API_BASE_URL

envsubst '${API_BASE_URL}' \
  < /opt/sandra-x-andreia/config.template.js \
  > /usr/share/nginx/html/config.js
