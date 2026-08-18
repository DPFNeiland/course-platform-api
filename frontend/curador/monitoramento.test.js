const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const source = fs.readFileSync(path.join(__dirname, 'curador.js'), 'utf8');

function response(body, status = 200) {
  return {
    ok: status >= 200 && status < 300,
    status,
    json: async () => body
  };
}

function deferred() {
  let resolve;
  const promise = new Promise(resolvePromise => {
    resolve = resolvePromise;
  });
  return { promise, resolve };
}

function element(tagName, textContent = '') {
  return {
    tagName: tagName.toUpperCase(),
    className: '',
    textContent,
    dataset: {},
    children: [],
    attributes: new Map([['aria-busy', 'true']]),
    appendChild(child) {
      this.children.push(child);
      return child;
    },
    replaceChildren(...children) {
      this.children = children;
    },
    removeAttribute(name) {
      this.attributes.delete(name);
    }
  };
}

function treeText(node) {
  return [node.textContent, ...(node.children || []).map(treeText)].join(' ');
}

function treeTags(node) {
  return [node.tagName, ...(node.children || []).flatMap(treeTags)].filter(Boolean);
}

function createHarness(responses = {}) {
  const listeners = {};
  const calls = [];
  const kpis = Array.from({ length: 4 }, () => element('div', '--'));
  const kpisSection = element('section');
  const graphs = element('section');
  const tableSection = element('section');
  const tbody = element('tbody');

  const document = {
    addEventListener(type, listener) {
      listeners[type] = listeners[type] || [];
      listeners[type].push(listener);
    },
    querySelector(selector) {
      const elements = {
        '.kpis': kpisSection,
        '.graphs': graphs,
        '.table-section': tableSection,
        '.table-section tbody': tbody
      };
      return elements[selector] || null;
    },
    querySelectorAll(selector) {
      if (selector === '.kpi-number') return kpis;
      return [];
    },
    createElement(tagName) {
      return element(tagName);
    }
  };
  const window = {
    APP_CONFIG: { API_BASE_URL: 'https://api.test' },
    location: { href: '' },
    jwtSession: {
      recoverSession: async () => ({ perfil: 'curador', nome: 'Curador' }),
      requireSession: () => ({ perfil: 'curador' }),
      authenticatedFetch: async (_baseUrl, requestPath) => {
        calls.push(requestPath);
        const configured = responses[requestPath];
        if (configured instanceof Promise) return configured;
        if (typeof configured === 'function') return configured();
        return configured || response([]);
      },
      logout() {}
    }
  };
  const context = {
    window,
    document,
    sessionStorage: { setItem() {} },
    location: { pathname: '/curador/monitoramento.html' },
    alert() {},
    console
  };

  vm.runInNewContext(source, context);

  return {
    calls,
    graphs,
    kpis,
    kpisSection,
    tableSection,
    tbody,
    start: () => listeners.DOMContentLoaded[0]()
  };
}

test('mantem placeholders ate os dados chegarem e depois apresenta valores reais', async () => {
  const courses = deferred();
  const harness = createHarness({
    '/curso': courses.promise,
    '/matricula': response([{
      id: 20,
      cursoId: 10,
      alunoId: 30,
      status: 'ATIVA',
      dataMatricula: '2026-08-18'
    }]),
    '/aluno': response([{ id: 30, nome: 'Aluna Real' }]),
    '/professor': response([{ id: 40, nome: 'Professor Real' }]),
    '/usuario': response([])
  });

  const loading = harness.start();
  await new Promise(resolve => setImmediate(resolve));

  assert.deepEqual(harness.kpis.map(kpi => kpi.textContent), ['--', '--', '--', '--']);
  assert.equal(harness.kpisSection.attributes.get('aria-busy'), 'true');

  courses.resolve(response([{
    id: 10,
    nome: 'Curso Real',
    status: 'APROVADO',
    professorId: 40
  }]));
  await loading;

  assert.deepEqual(harness.kpis.map(kpi => kpi.textContent), [1, 1, 1, 1]);
  assert.match(treeText(harness.tbody), /Curso Real/);
  assert.match(treeText(harness.tbody), /Aluna Real/);
  assert.match(treeText(harness.graphs), /Aprovados: 1/);
  assert.match(treeText(harness.graphs), /1 registro\(s\) reais/);
  assert.equal(harness.kpisSection.attributes.has('aria-busy'), false);
  assert.equal(harness.tableSection.attributes.has('aria-busy'), false);
  assert.equal(harness.graphs.attributes.has('aria-busy'), false);
});

test('listas vazias apresentam zeros e estados vazios reais', async () => {
  const harness = createHarness();

  await harness.start();

  assert.deepEqual(harness.kpis.map(kpi => kpi.textContent), [0, 0, 0, 0]);
  assert.match(treeText(harness.tbody), /Nenhuma matricula registrada/);
  assert.match(treeText(harness.graphs), /Aprovados: 0/);
  assert.match(treeText(harness.graphs), /0 registro\(s\) reais/);
});

test('falha na carga remove o loading e marca o monitoramento como indisponivel', async () => {
  const harness = createHarness({
    '/curso': () => Promise.reject(new Error('Falha de rede'))
  });

  await harness.start();

  assert.deepEqual(harness.kpis.map(kpi => kpi.textContent), ['--', '--', '--', '--']);
  assert.match(treeText(harness.tbody), /Dados de monitoramento indisponiveis/);
  assert.match(treeText(harness.graphs), /Monitoramento indisponivel/);
  assert.equal(harness.kpisSection.attributes.has('aria-busy'), false);
  assert.equal(harness.tableSection.attributes.has('aria-busy'), false);
  assert.equal(harness.graphs.attributes.has('aria-busy'), false);
});

test('nomes recebidos da API sao exibidos como texto sem criar HTML', async () => {
  const harness = createHarness({
    '/curso': response([{
      id: 10,
      nome: '<img src=x onerror=alert(1)>',
      status: 'APROVADO',
      professorId: 40
    }]),
    '/matricula': response([{ cursoId: 10, alunoId: 30, status: 'ATIVA' }]),
    '/aluno': response([{ id: 30, nome: '<script>alert(2)</script>' }])
  });

  await harness.start();

  assert.match(treeText(harness.tbody), /<img src=x onerror=alert\(1\)>/);
  assert.match(treeText(harness.tbody), /<script>alert\(2\)<\/script>/);
  assert.equal(treeTags(harness.tbody).includes('IMG'), false);
  assert.equal(treeTags(harness.tbody).includes('SCRIPT'), false);
});
