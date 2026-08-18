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
    '/professor': response([{ id: 40, nome: 'Professor Real' }])
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
  assert.deepEqual(harness.calls.sort(), ['/aluno', '/curso', '/matricula', '/professor']);
  assert.equal(harness.calls.includes('/usuario'), false);
});

test('listas vazias apresentam zeros e estados vazios reais', async () => {
  const harness = createHarness();

  await harness.start();

  assert.deepEqual(harness.kpis.map(kpi => kpi.textContent), [0, 0, 0, 0]);
  assert.match(treeText(harness.tbody), /Nenhuma matrícula registrada/);
  assert.match(treeText(harness.graphs), /Aprovados: 0/);
  assert.match(treeText(harness.graphs), /0 registro\(s\) reais/);
});

test('KPI de cursos conta apenas aprovados e o resumo separa os status reais', async () => {
  const harness = createHarness({
    '/curso': response([
      { id: 10, nome: 'Aprovado', status: 'APROVADO' },
      { id: 11, nome: 'Em avaliação', status: 'EM_AVALIACAO' },
      { id: 12, nome: 'Reavaliar', status: 'REAVALIAR' }
    ])
  });

  await harness.start();

  assert.deepEqual(harness.kpis.map(kpi => kpi.textContent), [1, 0, 0, 0]);
  assert.match(treeText(harness.graphs), /Aprovados: 1/);
  assert.match(treeText(harness.graphs), /Em avaliação: 1/);
  assert.match(treeText(harness.graphs), /Reavaliar: 1/);
});

test('falha em cursos preserva os demais indicadores e marca apenas os dados dependentes', async () => {
  const harness = createHarness({
    '/curso': () => Promise.reject(new Error('Falha de rede'))
  });

  await harness.start();

  assert.deepEqual(harness.kpis.map(kpi => kpi.textContent), ['--', 0, 0, 0]);
  assert.match(treeText(harness.tbody), /Nenhuma matrícula registrada/);
  assert.match(treeText(harness.graphs), /Dados de cursos indisponíveis/);
  assert.match(treeText(harness.graphs), /0 registro\(s\) reais/);
  assert.equal(harness.kpisSection.attributes.has('aria-busy'), false);
  assert.equal(harness.tableSection.attributes.has('aria-busy'), false);
  assert.equal(harness.graphs.attributes.has('aria-busy'), false);
});

test('falhas em alunos e professores nao apagam cursos e matriculas validos', async () => {
  const harness = createHarness({
    '/curso': response([{ id: 10, nome: 'Curso Real', status: 'APROVADO' }]),
    '/matricula': response([{ cursoId: 10, alunoId: 30, status: 'ATIVA' }]),
    '/aluno': () => Promise.reject(new Error('Alunos indisponíveis')),
    '/professor': () => Promise.reject(new Error('Professores indisponíveis'))
  });

  await harness.start();

  assert.deepEqual(harness.kpis.map(kpi => kpi.textContent), [1, '--', '--', 1]);
  assert.match(treeText(harness.tbody), /Curso Real/);
  assert.match(treeText(harness.tbody), /Aluno indisponível/);
  assert.match(treeText(harness.graphs), /Aprovados: 1/);
  assert.match(treeText(harness.graphs), /1 registro\(s\) reais/);
});

test('falha em matriculas nao apresenta zero nem impede os outros KPIs', async () => {
  const harness = createHarness({
    '/curso': response([{ id: 10, nome: 'Curso Real', status: 'APROVADO' }]),
    '/matricula': () => Promise.reject(new Error('Matrículas indisponíveis')),
    '/aluno': response([{ id: 30, nome: 'Aluna Real' }]),
    '/professor': response([{ id: 40, nome: 'Professor Real' }])
  });

  await harness.start();

  assert.deepEqual(harness.kpis.map(kpi => kpi.textContent), [1, 1, 1, '--']);
  assert.match(treeText(harness.tbody), /Matrículas indisponíveis/);
  assert.match(treeText(harness.graphs), /Dados de matrículas indisponíveis/);
});

test('falha em todos os endpoints encerra o loading sem apresentar zeros ficticios', async () => {
  const failure = () => Promise.reject(new Error('API indisponível'));
  const harness = createHarness({
    '/curso': failure,
    '/matricula': failure,
    '/aluno': failure,
    '/professor': failure
  });

  await harness.start();

  assert.deepEqual(harness.kpis.map(kpi => kpi.textContent), ['--', '--', '--', '--']);
  assert.match(treeText(harness.tbody), /Matrículas indisponíveis/);
  assert.match(treeText(harness.graphs), /Dados de cursos indisponíveis/);
  assert.match(treeText(harness.graphs), /Dados de matrículas indisponíveis/);
  assert.equal(harness.kpisSection.attributes.has('aria-busy'), false);
  assert.equal(harness.tableSection.attributes.has('aria-busy'), false);
  assert.equal(harness.graphs.attributes.has('aria-busy'), false);
});

test('payload de cursos fora do contrato nao derruba os demais dados validos', async () => {
  const harness = createHarness({
    '/curso': response({ id: 10, nome: 'Formato inválido' }),
    '/matricula': response([{ cursoId: 10, alunoId: 30, status: 'ATIVA' }]),
    '/aluno': response([{ id: 30, nome: 'Aluna Real' }]),
    '/professor': response([{ id: 40, nome: 'Professor Real' }])
  });

  await harness.start();

  assert.deepEqual(harness.kpis.map(kpi => kpi.textContent), ['--', 1, 1, 1]);
  assert.match(treeText(harness.tbody), /Curso indisponível/);
  assert.match(treeText(harness.tbody), /Aluna Real/);
  assert.match(treeText(harness.graphs), /Dados de cursos indisponíveis/);
  assert.match(treeText(harness.graphs), /1 registro\(s\) reais/);
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
