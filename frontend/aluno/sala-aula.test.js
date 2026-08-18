const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const source = fs.readFileSync(path.join(__dirname, 'aluno.js'), 'utf8');

function response(body, status = 200, { headers = {}, blob = body } = {}) {
  return {
    ok: status >= 200 && status < 300,
    status,
    json: async () => body,
    blob: async () => blob,
    headers: {
      get(name) {
        const entry = Object.entries(headers).find(([key]) => key.toLowerCase() === name.toLowerCase());
        return entry ? entry[1] : null;
      }
    }
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
    appendChild(child) {
      current.children.push(child);
      return child;
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
    },
    closest(selector) {
      return selector === '.download-material' && current.className.split(/\s+/).includes('download-material')
        ? current
        : null;
    },
    click() {
      current.clicked = true;
    },
    remove() {
      current.removed = true;
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

function createHarness({
  courses = response([]),
  enrollments = response([]),
  materials = response([]),
  download = response({}, 200),
  recoverSession = async () => ({ id: 7, perfil: 'aluno', nome: 'Aluna Real' }),
  search = '?cursoId=10'
} = {}) {
  const listeners = {};
  const calls = [];
  const alerts = [];
  const downloads = [];
  const objectUrls = [];
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
    body: element('body'),
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
      const created = element(tagName);
      if (tagName === 'a') downloads.push(created);
      return created;
    },
    createTextNode(text) {
      return element('#text', text);
    }
  };
  const responses = {
    '/curso': courses,
    '/matricula/me': enrollments,
    '/curso/10/materiais': materials,
    '/curso/10/materiais/2/arquivo': download
  };
  const window = {
    APP_CONFIG: { API_BASE_URL: 'https://api.test' },
    location: { href: '', search },
    jwtSession: {
      recoverSession,
      requireSession: () => ({ id: 7, perfil: 'aluno', nome: 'Aluna Real' }),
      authenticatedFetch: async (_baseUrl, requestPath, options) => {
        calls.push({ path: requestPath, options });
        const selected = responses[requestPath];
        return selected instanceof Promise ? selected : selected;
      },
      logout() {}
    },
    open() {}
  };

  const urlApi = {
    createObjectURL(blob) {
      objectUrls.push({ action: 'create', blob });
      return 'blob:material';
    },
    revokeObjectURL(url) {
      objectUrls.push({ action: 'revoke', url });
    }
  };

  vm.runInNewContext(source, {
    window,
    document,
    URL: urlApi,
    URLSearchParams,
    alert(message) { alerts.push(message); },
    console
  });

  return {
    alerts,
    calls,
    comments,
    completeButton,
    courseHeader,
    description,
    downloads,
    lessonTitle,
    location: window.location,
    materialGrid,
    objectUrls,
    progressFill,
    progressText,
    start: () => listeners.DOMContentLoaded[0](),
    click: target => listeners.click[0]({ target, preventDefault() {} })
  };
}

const course = { id: 10, nome: 'Curso Real', tipoCurso: 'ASSINCRONO', tipoAssinatura: 'COMUM', status: 'APROVADO' };
const activeEnrollment = { id: 20, alunoId: 7, cursoId: 10, status: 'EM_ANDAMENTO' };

test('carrega curso e materiais reais mantendo o skeleton durante a requisicao', async () => {
  const pending = deferred();
  const harness = createHarness({
    courses: response([course]),
    enrollments: response([activeEnrollment]),
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

  assert.deepEqual(harness.calls.map(call => call.path), ['/curso', '/matricula/me', '/curso/10/materiais']);
  assert.equal(harness.materialGrid.children.length, 2);
  assert.match(treeText(harness.materialGrid), /<img src=x>/);
  assert.equal(treeTags(harness.materialGrid).includes('IMG'), false);
  assert.equal(harness.materialGrid.children[0].children[2].href, 'https://material.test/aula');
  assert.equal(harness.materialGrid.children[1].children[2].dataset.downloadPath, '/curso/10/materiais/2/arquivo');
  assert.equal(harness.materialGrid.attributes.has('aria-busy'), false);
  assert.match(treeText(harness.comments), /Comentários em breve/);
});

test('curso encerrado apresenta progresso real e estado vazio de materiais', async () => {
  const harness = createHarness({
    courses: response([course]),
    enrollments: response([{ ...activeEnrollment, status: 'ENCERRADA' }])
  });
  await harness.start();

  assert.equal(harness.progressFill.style.width, '100%');
  assert.equal(harness.progressText.textContent, '100% concluído');
  assert.equal(harness.completeButton.textContent, 'Curso concluído');
  assert.equal(harness.completeButton.disabled, true);
  assert.match(treeText(harness.materialGrid), /Nenhum material disponível/);
});

test('aluno sem matricula nao consulta nem apresenta materiais de curso', async () => {
  const harness = createHarness({ courses: response([course]), enrollments: response([]) });
  await harness.start();

  assert.deepEqual(harness.calls.map(call => call.path), ['/curso', '/matricula/me']);
  assert.equal(harness.courseHeader.textContent, 'Nenhum curso selecionado');
  assert.equal(harness.completeButton.textContent, 'Matrícula necessária');
  assert.match(treeText(harness.materialGrid), /Matricule-se em um curso/);
});

test('payload de materiais fora do contrato encerra o loading com erro explicito', async () => {
  const harness = createHarness({
    courses: response([course]),
    enrollments: response([activeEnrollment]),
    materials: response({ id: 1, titulo: 'Formato incorreto' })
  });
  await harness.start();

  assert.match(treeText(harness.materialGrid), /Resposta de materiais inválida/);
  assert.equal(harness.materialGrid.attributes.has('aria-busy'), false);
});

test('erro da API de materiais substitui o skeleton por mensagem', async () => {
  const harness = createHarness({
    courses: response([course]),
    enrollments: response([activeEnrollment]),
    materials: response({ message: 'Materiais indisponíveis' }, 500)
  });
  await harness.start();

  assert.match(treeText(harness.materialGrid), /Materiais indisponíveis/);
  assert.equal(harness.materialGrid.attributes.has('aria-busy'), false);
});

test('falha de rede em materiais encerra o loading sem restaurar mocks', async () => {
  const harness = createHarness({
    courses: response([course]),
    enrollments: response([activeEnrollment]),
    materials: Promise.reject(new Error('Falha de rede'))
  });
  await harness.start();

  assert.match(treeText(harness.materialGrid), /Falha de rede/);
  assert.equal(harness.materialGrid.attributes.has('aria-busy'), false);
});

test('falha ao carregar cursos encerra todos os skeletons da sala', async () => {
  const harness = createHarness({
    courses: response({ message: 'Cursos indisponíveis' }, 500),
    enrollments: response([activeEnrollment])
  });
  await harness.start();

  assert.equal(harness.courseHeader.textContent, 'Sala de aula indisponível');
  assert.equal(harness.lessonTitle.textContent, 'Não foi possível carregar o curso');
  assert.match(treeText(harness.materialGrid), /Cursos indisponíveis/);
  assert.equal(harness.materialGrid.attributes.has('aria-busy'), false);
  assert.equal(harness.completeButton.disabled, true);
});

test('payload inválido de matrículas apresenta erro explícito e encerra loading', async () => {
  const harness = createHarness({
    courses: response([course]),
    enrollments: response({ id: 20 })
  });
  await harness.start();

  assert.match(treeText(harness.description), /Resposta de matrículas inválida/);
  assert.equal(harness.progressText.textContent, 'Progresso indisponível');
  assert.equal(harness.description.attributes.has('aria-busy'), false);
});

test('payload inválido de cursos apresenta erro explícito e encerra loading', async () => {
  const harness = createHarness({
    courses: response({ id: 10 }),
    enrollments: response([activeEnrollment])
  });
  await harness.start();

  assert.match(treeText(harness.description), /Resposta de cursos inválida/);
  assert.equal(harness.materialGrid.attributes.has('aria-busy'), false);
});

test('falha na recuperação da sessão encerra o loading da sala', async () => {
  const harness = createHarness({
    recoverSession: async () => { throw new Error('Sessão indisponível'); }
  });
  await harness.start();

  assert.match(treeText(harness.materialGrid), /Sessão indisponível/);
  assert.equal(harness.courseHeader.attributes.has('aria-busy'), false);
});

test('sessão ausente redireciona para login sem consultar dados', async () => {
  const harness = createHarness({ recoverSession: async () => null });
  await harness.start();

  assert.equal(harness.calls.length, 0);
  assert.equal(harness.location.href, '../index.html');
});

test('download de arquivo usa cliente autenticado e revoga a URL temporária', async () => {
  const file = { bytes: 'conteúdo real' };
  const harness = createHarness({
    courses: response([course]),
    enrollments: response([activeEnrollment]),
    materials: response([
      { id: 2, cursoId: 10, titulo: 'Apostila real', tipo: 'ARQUIVO', nomeArquivo: 'fallback.pdf' }
    ]),
    download: response({}, 200, {
      blob: file,
      headers: { 'Content-Disposition': 'attachment; filename="apostila.pdf"' }
    })
  });
  await harness.start();

  const button = harness.materialGrid.children[0].children[2];
  await harness.click(button);

  const call = harness.calls.at(-1);
  assert.equal(call.path, '/curso/10/materiais/2/arquivo');
  assert.equal(call.options.method, 'GET');
  assert.equal(harness.downloads.at(-1).download, 'apostila.pdf');
  assert.equal(harness.downloads.at(-1).clicked, true);
  assert.equal(harness.downloads.at(-1).removed, true);
  assert.deepEqual(harness.objectUrls, [
    { action: 'create', blob: file },
    { action: 'revoke', url: 'blob:material' }
  ]);
  assert.equal(button.disabled, false);
  assert.deepEqual(harness.alerts, []);
});

test('erro no download não abre arquivo e reabilita o botão', async () => {
  const harness = createHarness({
    courses: response([course]),
    enrollments: response([activeEnrollment]),
    materials: response([
      { id: 2, cursoId: 10, titulo: 'Apostila real', tipo: 'ARQUIVO', nomeArquivo: 'fallback.pdf' }
    ]),
    download: response({ erro: 'Acesso negado' }, 403)
  });
  await harness.start();

  const button = harness.materialGrid.children[0].children[2];
  await harness.click(button);

  assert.deepEqual(harness.alerts, ['Acesso negado']);
  assert.equal(harness.downloads.filter(item => item.clicked).length, 0);
  assert.equal(button.disabled, false);
});
