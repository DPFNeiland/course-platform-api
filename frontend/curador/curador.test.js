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

function element(tagName) {
  return {
    tagName: tagName.toUpperCase(),
    className: '',
    textContent: '',
    dataset: {},
    children: [],
    appendChild(child) {
      this.children.push(child);
      return child;
    },
    replaceChildren(...children) {
      this.children = children;
      this.innerHTML = '';
    }
  };
}

function treeText(node) {
  return [node.textContent, ...(node.children || []).map(treeText)].join(' ');
}

function treeTags(node) {
  return [node.tagName, ...(node.children || []).flatMap(treeTags)].filter(Boolean);
}

function createHarness(responses = {}, recoverSession) {
  const listeners = {};
  const calls = [];
  const attributes = new Map([
    ['aria-busy', 'true'],
    ['aria-label', 'Carregando cursos']
  ]);
  const container = element('div');
  container.innerHTML = '<div class="course-skeleton"></div>';
  container.removeAttribute = name => attributes.delete(name);

  const document = {
    addEventListener(type, listener) {
      listeners[type] = listeners[type] || [];
      listeners[type].push(listener);
    },
    querySelector(selector) {
      if (selector === '.courses-grid, [data-curator-courses]') return container;
      return null;
    },
    querySelectorAll() {
      return [];
    },
    createElement(tagName) {
      return element(tagName);
    }
  };
  const sessionStorage = { setItem() {} };
  const window = {
    APP_CONFIG: { API_BASE_URL: 'https://api.test' },
    location: { href: '' },
    jwtSession: {
      recoverSession: recoverSession || (async () => ({ perfil: 'curador', nome: 'Curador' })),
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
    sessionStorage,
    location: { pathname: '/curador/catalogo.html' },
    alert() {},
    console
  };

  vm.runInNewContext(source, context);

  return {
    attributes,
    calls,
    container,
    start: () => listeners.DOMContentLoaded[0]()
  };
}

test('mantem o skeleton enquanto GET /curso esta pendente e depois exibe os cursos reais', async () => {
  const courses = deferred();
  const harness = createHarness({
    '/curso': courses.promise,
    '/professor': response([{ id: 7, nome: 'Professora Real' }])
  });

  const loading = harness.start();
  await new Promise(resolve => setImmediate(resolve));

  assert.match(harness.container.innerHTML, /course-skeleton/);
  assert.equal(harness.attributes.get('aria-busy'), 'true');
  assert.deepEqual(harness.calls.sort(), ['/curso', '/professor']);

  courses.resolve(response([{
    id: 1,
    nome: 'Curso Real',
    professorId: 7,
    tipoCurso: 'ONLINE',
    tipoAssinatura: 'GRATUITO',
    status: 'APROVADO'
  }]));
  await loading;

  assert.match(treeText(harness.container), /Curso Real/);
  assert.doesNotMatch(harness.container.innerHTML, /course-skeleton/);
  assert.equal(harness.attributes.has('aria-busy'), false);
});

test('lista vazia encerra o loading e apresenta o estado vazio', async () => {
  const harness = createHarness({
    '/curso': response([]),
    '/professor': response([])
  });

  await harness.start();

  assert.match(treeText(harness.container), /Nenhum curso cadastrado/);
  assert.equal(harness.attributes.has('aria-busy'), false);
});

test('exibe os cursos sem aguardar GET /professor e atualiza o nome depois', async () => {
  const professores = deferred();
  const harness = createHarness({
    '/curso': response([{
      id: 1,
      nome: 'Curso Real',
      professorId: 7,
      tipoCurso: 'ONLINE',
      tipoAssinatura: 'GRATUITO',
      status: 'APROVADO'
    }]),
    '/professor': professores.promise
  });

  await harness.start();

  assert.doesNotMatch(harness.container.innerHTML, /course-skeleton/);
  assert.match(treeText(harness.container), /Curso Real/);
  assert.match(treeText(harness.container), /Professor #7/);
  assert.equal(harness.attributes.has('aria-busy'), false);

  professores.resolve(response([{ id: 7, nome: 'Professora Real' }]));
  await new Promise(resolve => setImmediate(resolve));

  assert.match(treeText(harness.container), /Professora Real/);
});

test('falha ao carregar professores nao impede a exibicao dos cursos', async () => {
  const harness = createHarness({
    '/curso': response([{
      id: 1,
      nome: 'Curso sem professor carregado',
      professorId: 8,
      tipoCurso: 'PRESENCIAL',
      tipoAssinatura: 'PAGO',
      status: 'APROVADO'
    }]),
    '/professor': () => Promise.reject(new Error('Falha ao carregar professores'))
  });

  await harness.start();
  await new Promise(resolve => setImmediate(resolve));

  assert.match(treeText(harness.container), /Curso sem professor carregado/);
  assert.match(treeText(harness.container), /Professor #8/);
  assert.equal(harness.attributes.has('aria-busy'), false);
});

test('renderiza nomes de curso e professor como texto sem criar HTML', async () => {
  const unsafeCourseName = '<img src=x onerror=alert(1)>';
  const unsafeProfessorName = '<script>alert(2)</script>';
  const harness = createHarness({
    '/curso': response([{
      id: 1,
      nome: unsafeCourseName,
      professorId: 7,
      tipoCurso: 'ONLINE',
      tipoAssinatura: 'GRATUITO',
      status: 'APROVADO'
    }]),
    '/professor': response([{ id: 7, nome: unsafeProfessorName }])
  });

  await harness.start();
  await new Promise(resolve => setImmediate(resolve));

  assert.match(treeText(harness.container), /<img src=x onerror=alert\(1\)>/);
  assert.match(treeText(harness.container), /<script>alert\(2\)<\/script>/);
  assert.equal(treeTags(harness.container).includes('IMG'), false);
  assert.equal(treeTags(harness.container).includes('SCRIPT'), false);
});

test('falha ao carregar cursos encerra o loading e apresenta mensagem como texto', async () => {
  const unsafeMessage = '<img src=x onerror=alert(1)>';
  const harness = createHarness({
    '/curso': response({ message: unsafeMessage }, 500),
    '/professor': response([])
  });

  await harness.start();

  assert.equal(harness.container.innerHTML, '');
  assert.equal(harness.container.children.length, 1);
  assert.equal(harness.container.children[0].className, 'empty-state');
  assert.equal(harness.container.children[0].textContent, unsafeMessage);
  assert.equal(harness.attributes.has('aria-busy'), false);
});

test('erro de rede encerra o loading e apresenta uma mensagem no catalogo', async () => {
  const harness = createHarness({
    '/curso': () => Promise.reject(new Error('Falha de rede')),
    '/professor': response([])
  });

  await harness.start();

  assert.equal(harness.container.children.length, 1);
  assert.equal(harness.container.children[0].textContent, 'Servico temporariamente indisponivel. Tente novamente.');
  assert.equal(harness.attributes.has('aria-busy'), false);
});

test('catalogo nao aguarda nem consulta matriculas para renderizar', async () => {
  const harness = createHarness({
    '/curso': response([]),
    '/professor': response([]),
    '/matricula': new Promise(() => {})
  });

  await harness.start();

  assert.equal(harness.calls.includes('/matricula'), false);
  assert.match(treeText(harness.container), /Nenhum curso cadastrado/);
});

test('falha ao recuperar a sessao encerra o loading sem consultar a API', async () => {
  const harness = createHarness({}, async () => {
    throw new Error('Servidor de sessão indisponível');
  });

  await harness.start();

  assert.deepEqual(harness.calls, []);
  assert.match(treeText(harness.container), /Servidor de sessão indisponível/);
  assert.equal(harness.attributes.has('aria-busy'), false);
});
