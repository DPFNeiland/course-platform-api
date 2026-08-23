const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

function execute(source) {
  const window = {};
  vm.runInNewContext(source, { window });
  return window.APP_CONFIG;
}

test('configuracao local fornece uma unica API_BASE_URL', () => {
  const source = fs.readFileSync(path.join(__dirname, 'config.js'), 'utf8');
  const config = execute(source);

  assert.equal(typeof config.API_BASE_URL, 'string');
  assert.notEqual(config.API_BASE_URL.trim(), '');
  assert.equal(Object.isFrozen(config), true);
});

test('template preserva o mesmo contrato da configuracao local', () => {
  const localSource = fs.readFileSync(path.join(__dirname, 'config.js'), 'utf8');
  const template = fs.readFileSync(path.join(__dirname, 'config.template.js'), 'utf8');
  const configuredSource = template.replace('${API_BASE_URL}', 'https://api-hml.example.test');
  const localConfig = execute(localSource);
  const config = execute(configuredSource);

  assert.deepEqual(Object.keys(config), Object.keys(localConfig));
  assert.equal(config.API_BASE_URL, 'https://api-hml.example.test');
});

test('container serve os arquivos mutaveis do frontend sem cache', () => {
  const dockerfile = fs.readFileSync(path.join(__dirname, 'Dockerfile'), 'utf8');
  const nginx = fs.readFileSync(path.join(__dirname, 'nginx.conf'), 'utf8');

  assert.match(dockerfile, /COPY nginx\.conf \/etc\/nginx\/conf\.d\/default\.conf/);
  assert.match(nginx, /location ~\* \\\.\(\?:html\|js\|css\)\$/);
  assert.match(nginx, /Cache-Control "no-store, no-cache, must-revalidate, proxy-revalidate, max-age=0" always/);
  assert.match(nginx, /etag off/);
});
