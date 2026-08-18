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
  const current = {
    tagName: tagName.toUpperCase(),
    className: '',
    textContent,
    dataset: {},
    style: {},
    children: [],
    attributes: new Map(),
    disabled: false,
    classList: {
      remove(className) {
        current.className = current.className
          .split(/\s+/)
          .filter(value => value && value !== className)
          .join(' ');
      }
    },
    append(...children) {
      current.children.push(...children);
    },
    replaceChildren(...children) {
      current.textContent = '';
      current.children = children;
    },
    removeAttribute(name) {
      current.attributes.delete(name);
    },
    matches() {
      return false;
    }
  };
  return current;
}

function treeText(node) {
  return [node.textContent, ...(node.children || []).map(treeText)].join(' ');
}

function treeTags(node) {
  return [node.tagName, ...(node.children || []).flatMap(treeTags)].filter(Boolean);
}

function createHarness({ courses = [], enrollments = [], materials = response([]), search = '?cursoId=10' } = {}) {
  const listeners = {};
  const calls = [];
  const courseHeader = element('span', 'Carregando curso...');
  courseHeader.attributes.set('aria-busy', 'true');
  const lessonTitle = element('h2', 'Carregando curso...');
  lessonTitle.attributes.set('aria-busy', 'true');
  const description = element('div', 'Carregando informações do curso...');
  description.attributes.set('aria-busy', 'true');
  const progressText = element('p', 'Carregando progresso...');
  progressText.attributes.set('aria-busy', 'true');
  const progressFill = element('div');
  progressFill.style.width = '0%';
  const completeButton = element('button', 'Carregando...');
  completeButton.className = 'btn-pill btn-primary complete-course-btn';
  completeButton.disabled = true;
  const comments = element('div');
  const materialGrid = element('div', 'skeleton');
  materialGrid.attributes.set('aria-busy', 'true');
  materialGrid.attributes.set('aria-label', 'Carregando materiais');
  const avatar = element('div');
  avatar.className = 'avatar avatar-skeleton';
  avatar.attributes.set('aria-label', 'Carregando perfil');

  const selectors = {
    '.lesson-title': lessonTitle,
    '.lesson-description': description,
    '.progress-text': progressText,
    '.lesson-content .progress-fill': progressFill,
    '.complete-course-btn': completeButton,
    '.comments-list': comments,
    '.support-materials .cards-grid': materialGrid
  };
  const document = {
    addEventListener(type, listener) {
      listeners[type] = listeners[type] || [];
      listeners[type].push(listener);
    },
    querySelector(selector) {
      return selectors[selector] || null;
    },
    querySelectorAll(selector) {
      if (selector === '[data-room-course-title]') return [courseHeader];
      if (selector === '.avatar') return [avatar];
      return [];
    },
    createElement(tagName) {
      return element(tagName);
    },
    createTextNode(text) {
      return element('#text', text);
    }
  };
  const responses = {
    '/curso': response(courses),
    '/matricula/me': response(enrollments),
    '/curso/10/materiais': materials
  };
  const window = {
    APP_CONFIG: { API_BASE_URL: 'https://api.test' },
    location: { href: '', search },
    jwtSession: {
      recoverSession: async () => ({ id: 7, perfil: 'aluno', nome: 'Aluna Real' }),
      requireSession: () => ({ id: 7, perfil: 'aluno', nome: 'Aluna Real' }),
      authenticatedFetch: async (_baseUrl, requestPath) => {
        calls.push(requestPath);
        const selected = responses[requestPath];
        return selected instanceof Promise ? selected : selected;
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
    calls,
    comments,
    completeButton,
    courseHeader,
    description,
    lessonTitle,
    materialGrid,
    progressFill,
    progressText,
    start: () => listeners.DOMContentLoaded[0]()
  };
}

const course = { id: 10, nome: 'Curso Real', tipoCurso: 'ASSINCRONO', tipoAssinatura: 'COMUM', status: 'APROVADO' };
const activeEnrollment = { id: 20, alunoId: 7, cursoId: 10, status: 'EM_ANDAMENTO' };

test('carrega curso e materiais reais mantendo o skeleton durante a requisicao', async () => {
  const pending = deferred();
  const harness = createHarness({
    courses: [course],
    enrollments: [activeEnrollment],
    materials: pending.promise
  });

  const loading = harness.start();
  await new Promise(resolve => setImmediate(resolve));

  assert.equal(harness.courseHeader.textContent, 'Curso Real');
  assert.equal(harness.lessonTitle.textContent, 'Curso Real');
  assert.equal(harness.materialGrid.attributes.get('aria-busy'), 'true');
  assert.equal(harness.progressFill.style.width, '0%');
  assert.match(harness.progressText.textContent, /Progresso detalhado em breve/);

  pending.resolve(response([
    { id: 1, cursoId: 10, titulo: '<img src=x>', tipo: 'LINK', url: 'https://material.test/aula' },
    { id: 2, cursoId: 10, titulo: 'Apostila real', tipo: 'ARQUIVO', nomeArquivo: 'aula.pdf' }
  ]));
  await loading;

  assert.deepEqual(harness.calls, ['/curso', '/matricula/me', '/curso/10/materiais']);
  assert.equal(harness.materialGrid.children.length, 2);
  assert.match(treeText(harness.materialGrid), /<img src=x>/);
  assert.equal(treeTags(harness.materialGrid).includes('IMG'), false);
  assert.equal(harness.materialGrid.children[0].children[2].href, 'https://material.test/aula');
  assert.equal(harness.materialGrid.children[1].children[2].href, 'https://api.test/curso/10/materiais/2/arquivo');
  assert.equal(harness.materialGrid.attributes.has('aria-busy'), false);
  assert.match(treeText(harness.comments), /Comentários em breve/);
});

test('curso encerrado apresenta progresso real e estado vazio de materiais', async () => {
  const harness = createHarness({
    courses: [course],
    enrollments: [{ ...activeEnrollment, status: 'ENCERRADA' }]
  });
  await harness.start();

  assert.equal(harness.progressFill.style.width, '100%');
  assert.equal(harness.progressText.textContent, '100% concluído');
  assert.equal(harness.completeButton.textContent, 'Curso concluído');
  assert.equal(harness.completeButton.disabled, true);
  assert.match(treeText(harness.materialGrid), /Nenhum material disponível/);
});

test('aluno sem matricula nao consulta nem apresenta materiais de curso', async () => {
  const harness = createHarness({ courses: [course], enrollments: [] });
  await harness.start();

  assert.deepEqual(harness.calls, ['/curso', '/matricula/me']);
  assert.equal(harness.courseHeader.textContent, 'Nenhum curso selecionado');
  assert.equal(harness.completeButton.textContent, 'Matrícula necessária');
  assert.match(treeText(harness.materialGrid), /Matricule-se em um curso/);
});

test('payload de materiais fora do contrato encerra o loading com erro explicito', async () => {
  const harness = createHarness({
    courses: [course],
    enrollments: [activeEnrollment],
    materials: response({ id: 1, titulo: 'Formato incorreto' })
  });
  await harness.start();

  assert.match(treeText(harness.materialGrid), /Resposta de materiais inválida/);
  assert.equal(harness.materialGrid.attributes.has('aria-busy'), false);
});

test('erro da API de materiais substitui o skeleton por mensagem', async () => {
  const harness = createHarness({
    courses: [course],
    enrollments: [activeEnrollment],
    materials: response({ message: 'Materiais indisponíveis' }, 500)
  });
  await harness.start();

  assert.match(treeText(harness.materialGrid), /Materiais indisponíveis/);
  assert.equal(harness.materialGrid.attributes.has('aria-busy'), false);
});

test('falha de rede em materiais encerra o loading sem restaurar mocks', async () => {
  const harness = createHarness({
    courses: [course],
    enrollments: [activeEnrollment],
    materials: Promise.reject(new Error('Falha de rede'))
  });
  await harness.start();

  assert.match(treeText(harness.materialGrid), /Falha de rede/);
  assert.equal(harness.materialGrid.attributes.has('aria-busy'), false);
});
