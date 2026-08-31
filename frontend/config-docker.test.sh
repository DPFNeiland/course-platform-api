#!/bin/sh
set -eu

script_dir="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
image="${CONFIG_TEST_IMAGE:-sandra-x-andreia-frontend-config-test}"
config_path="/usr/share/nginx/html/config.js"
container="sxa-config-test-$$"

cleanup() {
  docker rm -f "$container" >/dev/null 2>&1 || true
}

trap cleanup EXIT INT TERM

docker build -t "$image" "$script_dir" >/dev/null

docker_exec() {
  if [ -n "${MSYSTEM:-}" ]; then
    MSYS_NO_PATHCONV=1 docker exec "$@"
  else
    docker exec "$@"
  fi
}

read_config() {
  cleanup

  if [ -n "${MSYSTEM:-}" ]; then
    MSYS_NO_PATHCONV=1 docker run -d \
      --name "$container" \
      "$@" \
      "$image" >/dev/null
  else
    docker run -d \
      --name "$container" \
      "$@" \
      "$image" >/dev/null
  fi

  result=0
  output=""
  ready=false
  attempt=0

  while [ "$attempt" -lt 100 ]; do
    if docker_exec "$container" sh -c \
      '[ "$(cat /proc/1/comm 2>/dev/null)" = "nginx" ]' \
      >/dev/null 2>&1; then
      ready=true
      break
    fi

    running="$(
      docker inspect \
        -f '{{.State.Running}}' \
        "$container" 2>/dev/null ||
        printf 'false'
    )"

    [ "$running" = "true" ] || break

    attempt=$((attempt + 1))
    sleep 0.1
  done

  if [ "$ready" = "true" ]; then
    output="$(
      docker_exec "$container" cat "$config_path" 2>/dev/null
    )" || result=$?
  else
    result=1
  fi

  if [ "$result" -ne 0 ]; then
    docker logs "$container" >&2 || true
  fi

  cleanup

  printf '%s\n' "$output"
  return "$result"
}

assert_contains() {
  content="$1"
  expected="$2"
  description="$3"

  if ! printf '%s\n' "$content" | grep -Fq "$expected"; then
    echo "Falha: $description" >&2
    echo "Valor esperado: $expected" >&2
    echo "Configuracao obtida:" >&2
    printf '%s\n' "$content" >&2
    exit 1
  fi
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
    echo "Falha: $description nao apresentou a mensagem esperada." >&2
    echo "Mensagem esperada: $expected_message" >&2
    echo "Saida obtida:" >&2
    printf '%s\n' "$errors" >&2
    exit 1
  fi
}

configured="$(
  read_config \
    -e APP_ENV=staging \
    -e API_BASE_URL=https://api-hml.example.test
)"

assert_contains \
  "$configured" \
  "API_BASE_URL: 'https://api-hml.example.test'" \
  "a URL configurada para staging nao foi gerada corretamente."

normalized="$(
  read_config \
    -e APP_ENV=production \
    -e API_BASE_URL=https://api.example.test///
)"

assert_contains \
  "$normalized" \
  "API_BASE_URL: 'https://api.example.test'" \
  "as barras finais da URL nao foram normalizadas."

read_config \
  -e APP_ENV=development \
  >/dev/null

development="$(
  read_config \
    -e APP_ENV=development \
    -e API_BASE_URL=http://api-dev.example.test:8081/v1/
)"

assert_contains \
  "$development" \
  "API_BASE_URL: 'http://api-dev.example.test:8081/v1'" \
  "a URL HTTP de desenvolvimento nao foi gerada corretamente."

assert_invalid \
  "API_BASE_URL e obrigatoria" \
  "URL ausente em producao" \
  -e APP_ENV=production

assert_invalid \
  "deve utilizar HTTPS" \
  "HTTP em producao" \
  -e APP_ENV=production \
  -e API_BASE_URL=http://api.example.test

assert_invalid \
  "API_BASE_URL invalida" \
  "URL sem protocolo" \
  -e APP_ENV=staging \
  -e API_BASE_URL=api.example.test

assert_invalid \
  "API_BASE_URL invalida" \
  "URL com espaco" \
  -e APP_ENV=staging \
  -e "API_BASE_URL=https://api example.test"

assert_invalid \
  "API_BASE_URL invalida" \
  "URL com aspas" \
  -e APP_ENV=staging \
  -e "API_BASE_URL=https://api.example.test/';alert(1)//"

assert_invalid \
  "API_BASE_URL invalida" \
  "hostname malformado" \
  -e APP_ENV=staging \
  -e API_BASE_URL=https://...

assert_invalid \
  "porta deve estar" \
  "porta acima do limite" \
  -e APP_ENV=staging \
  -e API_BASE_URL=https://api.example.test:70000

assert_invalid \
  "API_BASE_URL invalida" \
  "URL com query" \
  -e APP_ENV=staging \
  -e "API_BASE_URL=https://api.example.test?version=1"

echo "Configuracao Docker validada com sucesso."