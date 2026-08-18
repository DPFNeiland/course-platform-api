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
  if [ "$result" -ne 0 ]; then
    docker logs "$container" >&2 || true
  fi
  docker rm -f "$container" >/dev/null 2>&1 || true
  printf '%s\n' "$output"
  return "$result"
}

assert_invalid() {
  expected_message="$1"
  description="$2"
  shift 2
  if errors="$(read_config "$@" 2>&1 >/dev/null)"; then
    echo "Falha: $description deveria impedir a inicializacao." >&2
    exit 1
  fi
  if ! printf '%s\n' "$errors" | grep -Fq "$expected_message"; then
    echo "Falha: $description nao apresentou a mensagem esperada: $expected_message" >&2
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

development="$(read_config \
  -e APP_ENV=development \
  -e API_BASE_URL=http://api-dev.example.test:8081/v1/)"
printf '%s\n' "$development" | grep -q "API_BASE_URL: 'http://api-dev.example.test:8081/v1'"

assert_invalid "API_BASE_URL e obrigatoria" "URL ausente em producao" -e APP_ENV=production
assert_invalid "deve utilizar HTTPS" "HTTP em producao" -e APP_ENV=production -e API_BASE_URL=http://api.example.test
assert_invalid "API_BASE_URL invalida" "URL sem protocolo" -e APP_ENV=staging -e API_BASE_URL=api.example.test
assert_invalid "API_BASE_URL invalida" "URL com espaco" -e APP_ENV=staging -e "API_BASE_URL=https://api example.test"
assert_invalid "API_BASE_URL invalida" "URL com aspas" -e APP_ENV=staging -e "API_BASE_URL=https://api.example.test/';alert(1)//"
assert_invalid "API_BASE_URL invalida" "hostname malformado" -e APP_ENV=staging -e API_BASE_URL=https://...
assert_invalid "porta deve estar" "porta acima do limite" -e APP_ENV=staging -e API_BASE_URL=https://api.example.test:70000
assert_invalid "API_BASE_URL invalida" "URL com query" -e APP_ENV=staging -e "API_BASE_URL=https://api.example.test?version=1"

echo "Configuracao Docker validada com sucesso."
