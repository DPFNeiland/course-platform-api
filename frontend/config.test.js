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
  assert.equal(Object.isFrozen(config), true);
});

test('template aceita URL diferente por configuracao de deploy', () => {
  const template = fs.readFileSync(path.join(__dirname, 'config.template.js'), 'utf8');
  const configuredSource = template.replace('${API_BASE_URL}', 'https://api-hml.example.test');
  const config = execute(configuredSource);

  assert.equal(config.API_BASE_URL, 'https://api-hml.example.test');
});
