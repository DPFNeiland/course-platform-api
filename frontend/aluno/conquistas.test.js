const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const source = fs.readFileSync(path.join(__dirname, 'aluno.js'), 'utf8');

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
    attributes: new Map(),
    classList: {
      remove(className) {
        this.owner.className = this.owner.className
          .split(/\s+/)
          .filter(current => current && current !== className)
          .join(' ');
      },
      owner: null
    },
    appendChild(child) {
      this.children.push(child);
      return child;
    },
    append(...children) {
      this.children.push(...children);
    },
    replaceChildren(...children) {
      this.textContent = '';
      this.children = children;
    },
    removeAttribute(name) {
      this.attributes.delete(name);
    },
    matches() {
      return false;
    }
  };
}

function treeText(node) {
  return [node.textContent, ...(node.children || []).map(treeText)].join(' ');
}

function treeTags(node) {
  return [node.tagName, ...(node.children || []).flatMap(treeTags)].filter(Boolean);
}

function createHarness(matriculaResponse = response([]), studentName = 'Aluna Real', recoverSession) {
  const listeners = {};
  const calls = [];
  const grid = element('section', 'skeleton');
  grid.attributes.set('aria-busy', 'true');
  grid.attributes.set('aria-label', 'Carregando conquistas');
  const rankingSection = element('section');
  rankingSection.attributes.set('aria-busy', 'true');
  rankingSection.attributes.set('aria-label', 'Carregando pontuacao do aluno');
  const ranking = element('div', 'skeleton');
  const avatar = element('div');
  avatar.className = 'avatar avatar-skeleton';
  avatar.attributes.set('aria-label', 'Carregando perfil');
  avatar.classList.owner = avatar;

  const document = {
    addEventListener(type, listener) {
      listeners[type] = listeners[type] || [];
      listeners[type].push(listener);
    },
    querySelector(selector) {
      return {
        '.achievements-grid': grid,
        '.ranking-section': rankingSection,
        '.ranking': ranking
      }[selector] || null;
    },
    querySelectorAll() {
      return arguments[0] === '.avatar' ? [avatar] : [];
    },
    createElement(tagName) {
      return element(tagName);
    },
    createTextNode(text) {
      return element('#text', text);
    }
  };
  const window = {
    APP_CONFIG: { API_BASE_URL: 'https://api.test' },
    location: { href: '', search: '' },
    jwtSession: {
      recoverSession: recoverSession || (async () => ({ id: 7, perfil: 'aluno', nome: studentName })),
      requireSession: () => ({ id: 7, perfil: 'aluno', nome: studentName }),
      authenticatedFetch: async (_baseUrl, requestPath) => {
        calls.push(requestPath);
        return matriculaResponse instanceof Promise ? matriculaResponse : matriculaResponse;
      },
      logout() {}
    },
    open() {}
  };

  vm.runInNewContext(source, {
    window,
    document,
    URLSearchParams,
    alert() {},
    console
  });

  return {
    avatar,
    calls,
    grid,
    ranking,
    rankingSection,
    window,
    start: () => listeners.DOMContentLoaded[0]()
  };
}

test('mantem o skeleton ate carregar somente as matriculas do aluno autenticado', async () => {
  const pending = deferred();
  const harness = createHarness(pending.promise);

  const loading = harness.start();
  await new Promise(resolve => setImmediate(resolve));

  assert.equal(harness.grid.attributes.get('aria-busy'), 'true');
  assert.equal(harness.rankingSection.attributes.get('aria-busy'), 'true');

  pending.resolve(response([
    { id: 1, alunoId: 7, cursoId: 10, status: 'ATIVA' },
    { id: 2, alunoId: 7, cursoId: 11, status: 'ENCERRADA' },
    { id: 3, alunoId: 99, cursoId: 12, status: 'ATIVA' }
  ]));
  await loading;

  assert.deepEqual(harness.calls, ['/matricula/me']);
  assert.equal(harness.grid.children.length, 3);
  assert.match(treeText(harness.grid), /2 matrícula\(s\)/);
  assert.match(treeText(harness.grid), /1 em andamento/);
  assert.match(treeText(harness.grid), /1 encerrado\(s\)/);
  assert.match(treeText(harness.ranking), /Aluna Real/);
  assert.match(treeText(harness.ranking), /200 pts/);
  assert.match(treeText(harness.ranking), /—/);
  assert.equal(harness.grid.attributes.has('aria-busy'), false);
  assert.equal(harness.rankingSection.attributes.has('aria-busy'), false);
  assert.equal(harness.avatar.textContent, 'AL');
  assert.equal(harness.avatar.className, 'avatar');
  assert.equal(harness.avatar.attributes.has('aria-label'), false);
});

test('lista vazia exibe tres indicadores zerados e pontuacao zero', async () => {
  const harness = createHarness();
  await harness.start();

  assert.equal(harness.grid.children.length, 3);
  assert.match(treeText(harness.grid), /0 matrícula\(s\)/);
  assert.match(treeText(harness.grid), /0 em andamento/);
  assert.match(treeText(harness.grid), /0 encerrado\(s\)/);
  assert.match(treeText(harness.ranking), /0 pts/);
});

test('erro da API encerra o loading e apresenta uma mensagem explicita', async () => {
  const harness = createHarness(response({ message: 'API indisponivel' }, 500));
  await harness.start();

  assert.match(treeText(harness.grid), /API indisponivel/);
  assert.match(treeText(harness.ranking), /API indisponivel/);
  assert.equal(harness.grid.attributes.has('aria-busy'), false);
  assert.equal(harness.rankingSection.attributes.has('aria-busy'), false);
});

test('payload fora do contrato e tratado como erro sem loading infinito', async () => {
  const harness = createHarness(response({ id: 1, status: 'ATIVA' }));
  await harness.start();

  assert.match(treeText(harness.grid), /Resposta de matrículas inválida/);
  assert.equal(harness.grid.attributes.has('aria-busy'), false);
});

test('nome recebido da sessao e exibido como texto sem criar HTML', async () => {
  const harness = createHarness(response([]), '<script>alert(1)</script>');
  await harness.start();

  assert.match(treeText(harness.ranking), /<script>alert\(1\)<\/script>/);
  assert.equal(treeTags(harness.ranking).includes('SCRIPT'), false);
});

test('falha ao recuperar a sessao encerra os skeletons com mensagem explicita', async () => {
  const harness = createHarness(response([]), 'Aluna Real', async () => {
    throw new Error('Nao foi possivel recuperar a sessao.');
  });
  await harness.start();

  assert.deepEqual(harness.calls, []);
  assert.match(treeText(harness.grid), /Nao foi possivel recuperar a sessao/);
  assert.match(treeText(harness.ranking), /Nao foi possivel recuperar a sessao/);
  assert.equal(harness.grid.attributes.has('aria-busy'), false);
  assert.equal(harness.rankingSection.attributes.has('aria-busy'), false);
  assert.equal(harness.avatar.className, 'avatar');
  assert.equal(harness.avatar.attributes.has('aria-label'), false);
});

test('sessao ausente redireciona ao login sem consultar matriculas', async () => {
  const harness = createHarness(response([]), 'Aluna Real', async () => null);
  await harness.start();

  assert.deepEqual(harness.calls, []);
  assert.equal(harness.avatar.textContent, '');
  assert.equal(harness.window.location.href, '../index.html');
});
