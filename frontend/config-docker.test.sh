#!/bin/sh
set -eu

script_dir="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
image="${CONFIG_TEST_IMAGE:-sandra-x-andreia-frontend-config-test}"
config_path="/usr/share/nginx/html/config.js"
container="sxa-config-test-$$"

docker build -t "$image" "$script_dir" >/dev/null

read_config() {
  docker rm -f "$container" >/dev/null 2>&1 || true
  if [ -n "${MSYSTEM:-}" ]; then
    MSYS_NO_PATHCONV=1 docker run -d --name "$container" "$@" "$image" >/dev/null
    result=0
    output="$(MSYS_NO_PATHCONV=1 docker exec "$container" cat "$config_path" 2>/dev/null)" || result=$?
  else
    docker run -d --name "$container" "$@" "$image" >/dev/null
    result=0
    output="$(docker exec "$container" cat "$config_path" 2>/dev/null)" || result=$?
  fi
  docker rm -f "$container" >/dev/null 2>&1 || true
  printf '%s\n' "$output"
  return "$result"
}

assert_invalid() {
  description="$1"
  shift
  if read_config "$@" >/dev/null 2>&1; then
    echo "Falha: $description deveria impedir a inicializacao." >&2
    exit 1
  fi
}

configured="$(read_config \
  -e APP_ENV=staging \
  -e API_BASE_URL=https://api-hml.example.test)"
printf '%s\n' "$configured" | grep -q "API_BASE_URL: 'https://api-hml.example.test'"

normalized="$(read_config \
  -e APP_ENV=production \
  -e API_BASE_URL=https://api.example.test///)"
printf '%s\n' "$normalized" | grep -q "API_BASE_URL: 'https://api.example.test'"

read_config -e APP_ENV=development >/dev/null

assert_invalid "URL ausente em producao" -e APP_ENV=production
assert_invalid "URL sem protocolo" -e APP_ENV=staging -e API_BASE_URL=api.example.test
assert_invalid "URL com espaco" -e APP_ENV=staging -e "API_BASE_URL=https://api example.test"
assert_invalid "URL com aspas" -e APP_ENV=staging -e "API_BASE_URL=https://api.example.test/';alert(1)//"

echo "Configuracao Docker validada com sucesso."
