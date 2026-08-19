const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const source = fs.readFileSync(path.join(__dirname, 'dashboard-ui.js'), 'utf8');

function loadingElement() {
  const attributes = new Map([
    ['aria-busy', 'true'],
    ['aria-label', 'Carregando conteúdo'],
    ['aria-hidden', 'true']
  ]);
  return {
    className: 'card loading-skeleton loading-skeleton-card preserved-class',
    attributes,
    removeAttribute(name) {
      attributes.delete(name);
    }
  };
}

test('finaliza o skeleton compartilhado e torna o conteúdo real acessível', () => {
  const window = {};
  vm.runInNewContext(source, { window });
  const element = loadingElement();

  window.dashboardUI.finishLoading(element);

  assert.equal(element.className, 'card preserved-class');
  assert.equal(element.attributes.has('aria-busy'), false);
  assert.equal(element.attributes.has('aria-label'), false);
  assert.equal(element.attributes.has('aria-hidden'), false);
});

test('aceita elemento ausente sem interromper a renderização', () => {
  const window = {};
  vm.runInNewContext(source, { window });

  assert.doesNotThrow(() => window.dashboardUI.finishLoading(null));
});
