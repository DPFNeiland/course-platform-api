const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const uiSource = fs.readFileSync(path.join(__dirname, 'dashboard-ui.js'), 'utf8');

function response(body, status = 200) {
  return { ok: status >= 200 && status < 300, status, json: async () => body };
}

function deferred() {
  let resolve;
  const promise = new Promise(resolvePromise => { resolve = resolvePromise; });
  return { promise, resolve };
}

function element(tagName, textContent = '') {
  const listeners = {};
  const node = {
    tagName: tagName.toUpperCase(),
    className: '',
    textContent,
    dataset: {},
    children: [],
    attributes: new Map(),
    appendChild(child) { this.children.push(child); return child; },
    append(...children) { this.children.push(...children); },
    replaceChildren(...children) { this.textContent = ''; this.children = children; },
    removeAttribute(name) { this.attributes.delete(name); },
    setAttribute(name, value) { this.attributes.set(name, String(value)); },
    addEventListener(type, listener) {
      listeners[type] = listeners[type] || [];
      listeners[type].push(listener);
    },
    async dispatch(type, event = {}) {
      for (const listener of listeners[type] || []) await listener(event);
    },
    matches() { return false; },
    closest() { return null; },
    querySelector() { return null; }
  };
  node.classList = {
    remove(className) {
      node.className = node.className
        .split(/\s+/)
        .filter(current => current && current !== className)
        .join(' ');
    },
    contains(className) {
      return node.className.split(/\s+/).includes(className);
    }
  };
  return node;
}

function treeText(node) {
  return [node.textContent, ...(node.children || []).map(treeText)].join(' ');
}

function treeTags(node) {
  return [node.tagName, ...(node.children || []).flatMap(treeTags)].filter(Boolean);
}

function execute(scriptPath, document, window) {
  const listeners = {};
  document.addEventListener = (type, listener) => {
    listeners[type] = listeners[type] || [];
    listeners[type].push(listener);
  };
  const context = {
    window,
    document,
    location: { pathname: scriptPath.includes('curador') ? '/curador/dashboard.html' : '' },
    sessionStorage: { setItem() {} },
    URLSearchParams,
    alert() {},
    console
  };
  vm.runInNewContext(uiSource, context);
  vm.runInNewContext(fs.readFileSync(path.join(__dirname, scriptPath), 'utf8'), context);
  return () => listeners.DOMContentLoaded[0]();
}

function createStudentDashboardHarness({
  courseResponse = response([{ id: 10, nome: 'Curso Real', professorId: 3, status: 'APROVADO' }]),
  enrollmentResponse = response([{ id: 20, alunoId: 7, cursoId: 10, status: 'ATIVA' }]),
  recoverSession = async () => ({ id: 7, perfil: 'aluno', nome: 'Aluna Real' })
} = {}) {
  const gamification = element('p');
  gamification.className = 'loading-skeleton loading-skeleton-text';
  gamification.attributes.set('aria-hidden', 'true');
  const courseList = element('ul');
  const courseCard = element('div');
  courseCard.className = 'card loading-skeleton';
  courseCard.attributes.set('aria-busy', 'true');
  courseList.closest = selector => selector === '.card' ? courseCard : null;
  const gamificationCard = element('div');
  gamificationCard.className = 'card gamificacao';
  gamificationCard.attributes.set('aria-busy', 'true');
  const name = element('span');
  const avatar = element('div');
  avatar.className = 'avatar loading-skeleton loading-skeleton-avatar';
  const catalog = element('div');
  catalog.dataset.limit = '6';
  catalog.attributes.set('aria-busy', 'true');
  catalog.appendChild(element('div', 'skeleton'));
  const progress = element('div');
  progress.attributes.set('aria-busy', 'true');
  progress.appendChild(element('div', 'skeleton'));
  const calls = [];

  const document = {
    querySelector(selector) {
      return {
        '[data-dashboard-gamification]': gamification,
        '[data-dashboard-course-list]': courseList,
        '.gamificacao': gamificationCard,
        '[data-student-progress]': progress
      }[selector] || null;
    },
    querySelectorAll(selector) {
      return {
        '[data-approved-courses]': [catalog],
        '[data-approved-courses], [data-student-progress]': [catalog, progress],
        '.achievements .badge': [],
        '[data-field="nomeAluno"]': [name],
        '.avatar': [avatar]
      }[selector] || [];
    },
    createElement: tagName => element(tagName),
    createTextNode: text => element('#text', text)
  };
  const window = {
    APP_CONFIG: { API_BASE_URL: 'https://api.test' },
    location: { href: '', search: '' },
    jwtSession: {
      recoverSession,
      requireSession: () => ({ id: 7, perfil: 'aluno' }),
      authenticatedFetch: async (_base, requestPath) => {
        calls.push(requestPath);
        return requestPath === '/curso/disponiveis' ? courseResponse : enrollmentResponse;
      },
      logout() {}
    },
    open() {}
  };

  return {
    avatar,
    calls,
    catalog,
    courseCard,
    courseList,
    gamification,
    gamificationCard,
    name,
    progress,
    start: execute('aluno/aluno.js', document, window)
  };
}

test('dashboard do aluno encerra skeleton com dados reais e sem pontuacao provisoria', async () => {
  const harness = createStudentDashboardHarness();

  await harness.start();

  assert.equal(harness.name.textContent, 'Aluna Real');
  assert.equal(harness.gamification.textContent, 'Gamificação em breve.');
  assert.equal(harness.gamification.attributes.has('aria-hidden'), false);
  assert.doesNotMatch(treeText(harness.gamificationCard), /pontos/i);
  assert.match(treeText(harness.courseList), /Curso Real/);
  assert.match(treeText(harness.catalog), /Curso Real/);
  assert.match(treeText(harness.progress), /Curso Real/);
  assert.equal(harness.courseCard.attributes.has('aria-busy'), false);
});

test('dashboard do aluno mantém skeleton enquanto a API está pendente', async () => {
  const courses = deferred();
  const harness = createStudentDashboardHarness({ courseResponse: courses.promise });

  const loading = harness.start();
  await new Promise(resolve => setImmediate(resolve));

  assert.equal(harness.catalog.attributes.get('aria-busy'), 'true');
  assert.match(treeText(harness.catalog), /skeleton/);

  courses.resolve(response([]));
  await loading;
  assert.equal(harness.catalog.attributes.has('aria-busy'), false);
  assert.match(treeText(harness.catalog), /Nenhum curso aprovado disponível/);
});

test('dashboard do aluno renderiza nome de curso malicioso somente como texto', async () => {
  const maliciousName = '<img src=x onerror=alert(1)>';
  const harness = createStudentDashboardHarness({
    courseResponse: response([{ id: 10, nome: maliciousName, professorId: 3, status: 'APROVADO' }])
  });

  await harness.start();

  assert.match(treeText(harness.catalog), /<img src=x onerror=alert\(1\)>/);
  assert.equal(treeTags(harness.catalog).includes('IMG'), false);
});

test('dashboard do aluno trata mensagem de erro maliciosa como texto', async () => {
  const maliciousMessage = '<script>alert(1)</script>';
  const harness = createStudentDashboardHarness({
    courseResponse: response({ message: maliciousMessage }, 500)
  });

  await harness.start();

  assert.match(treeText(harness.catalog), /<script>alert\(1\)<\/script>/);
  assert.equal(treeTags(harness.catalog).includes('SCRIPT'), false);
  assert.equal(harness.catalog.attributes.has('aria-busy'), false);
  assert.equal(harness.progress.attributes.has('aria-busy'), false);
});

test('dashboard do aluno encerra loading para payload inválido e falha de sessão', async () => {
  const invalidPayload = createStudentDashboardHarness({ courseResponse: response({ id: 10 }) });
  await invalidPayload.start();
  assert.match(treeText(invalidPayload.catalog), /Resposta de cursos inválida/);
  assert.equal(invalidPayload.catalog.attributes.has('aria-busy'), false);

  const sessionFailure = createStudentDashboardHarness({
    recoverSession: async () => { throw new Error('Sessão indisponível'); }
  });
  await sessionFailure.start();
  assert.deepEqual(sessionFailure.calls, []);
  assert.match(treeText(sessionFailure.catalog), /Sessão indisponível/);
});

function createCuratorDashboardHarness({
  responses = {
    '/curso': response([{ id: 1, status: 'EM_AVALIACAO' }]),
    '/matricula': response([{ id: 1 }, { id: 2 }])
  },
  recoverSession = async () => ({ id: 2, perfil: 'curador', nome: 'Curadora Real' })
} = {}) {
  const name = element('span');
  const avatar = element('div');
  const pendingSummary = element('p');
  const enrollmentSummary = element('p');
  for (const summary of [pendingSummary, enrollmentSummary]) {
    summary.className = 'loading-skeleton loading-skeleton-text';
    summary.attributes.set('aria-hidden', 'true');
  }
  const card = (title, summary) => ({
    querySelector: selector => selector === 'h3' ? element('h3', title) : summary
  });
  const cards = [card('Aprovar Cursos', pendingSummary), card('Monitorar', enrollmentSummary)];
  const calls = [];

  const document = {
    querySelector(selector) {
      return selector === '.courses-grid, [data-curator-courses]' ? null : null;
    },
    querySelectorAll(selector) {
      return {
        '.cards-section .card': cards,
        '[data-curator-dashboard-summary]': [pendingSummary, enrollmentSummary],
        '[data-field="nomeCurador"]': [name],
        '.avatar': [avatar]
      }[selector] || [];
    },
    createElement: tagName => element(tagName)
  };
  const window = {
    APP_CONFIG: { API_BASE_URL: 'https://api.test' },
    location: { href: '' },
    jwtSession: {
      recoverSession,
      requireSession: () => ({ perfil: 'curador' }),
      authenticatedFetch: async (_base, requestPath) => {
        calls.push(requestPath);
        return responses[requestPath] || response([]);
      },
      logout() {}
    }
  };

  return {
    avatar,
    calls,
    enrollmentSummary,
    name,
    pendingSummary,
    start: execute('curador/curador.js', document, window)
  };
}

test('dashboard do curador apresenta identidade e contagens reais', async () => {
  const harness = createCuratorDashboardHarness();

  await harness.start();

  assert.equal(harness.name.textContent, 'Curadora Real');
  assert.equal(harness.avatar.textContent, 'CU');
  assert.match(harness.pendingSummary.textContent, /^1 registro/);
  assert.match(harness.enrollmentSummary.textContent, /^2 registro/);
  assert.equal(harness.pendingSummary.attributes.has('aria-hidden'), false);
});

test('dashboard do curador cobre loading, listas vazias e falhas', async () => {
  const pendingCourses = deferred();
  const pending = createCuratorDashboardHarness({ responses: {
    '/curso': pendingCourses.promise,
    '/matricula': response([])
  } });
  const loading = pending.start();
  await new Promise(resolve => setImmediate(resolve));
  assert.match(pending.pendingSummary.className, /loading-skeleton/);
  pendingCourses.resolve(response([]));
  await loading;
  assert.match(pending.pendingSummary.textContent, /^0 registro/);
  assert.match(pending.enrollmentSummary.textContent, /^0 registro/);

  const apiError = createCuratorDashboardHarness({ responses: {
    '/curso': response({ message: 'Cursos indisponíveis' }, 500),
    '/matricula': response([])
  } });
  await apiError.start();
  assert.match(apiError.pendingSummary.textContent, /Cursos indisponíveis/);
  assert.doesNotMatch(apiError.pendingSummary.className, /loading-skeleton/);

  const sessionError = createCuratorDashboardHarness({
    recoverSession: async () => { throw new Error('Sessão indisponível'); }
  });
  await sessionError.start();
  assert.deepEqual(sessionError.calls, []);
  assert.match(sessionError.pendingSummary.textContent, /Sessão indisponível/);
});

function createProfessorDashboardHarness({
  responses = {
    '/curso/professor/7': response([
      { id: 1, professorId: 7, nome: 'Curso Próprio', status: 'APROVADO' }
    ]),
    '/matricula/curso/1': response([{ id: 1, cursoId: 1 }])
  },
  recoverSession = async () => ({ id: 7, perfil: 'professor', nome: 'Professora Real' })
} = {}) {
  const values = Array.from({ length: 4 }, () => {
    const value = element('div');
    value.className = 'value loading-skeleton loading-skeleton-text';
    value.attributes.set('aria-hidden', 'true');
    return value;
  });
  const labels = Array.from({ length: 4 }, () => element('div'));
  const activities = element('div');
  const charts = element('div');
  const kpiGrid = element('div');
  const name = element('span');
  const avatar = element('div');
  const courseForm = element('form');
  const courseSubmit = element('button', 'Carregando sessão...');
  courseSubmit.disabled = true;
  courseForm.querySelector = selector => selector === '.course-submit' ? courseSubmit : null;
  const courseName = element('input');
  const courseSubscription = element('select');
  const courseType = element('select');
  const courseFeedback = element('p');
  courseForm.reset = () => {};
  const calls = [];
  const requests = [];

  const document = {
    querySelector(selector) {
      return {
        '.activities-grid': activities,
        '.charts-grid': charts,
        '.kpi-grid': kpiGrid,
        '#courseForm': courseForm
      }[selector] || null;
    },
    querySelectorAll(selector) {
      return {
        '.kpi-card .value': values,
        '.kpi-card .label': labels,
        '[data-field="nomeProfessor"]': [name],
        '.avatar': [avatar],
        '.cards-grid, .horizontal-grid': []
      }[selector] || [];
    },
    getElementById(id) {
      return {
        courseForm,
        'course-nome': courseName,
        'course-tipoAssinatura': courseSubscription,
        'course-tipoCurso': courseType,
        'course-feedback': courseFeedback
      }[id] || null;
    },
    createElement: tagName => element(tagName)
  };
  const window = {
    APP_CONFIG: { API_BASE_URL: 'https://api.test' },
    location: { href: '' },
    jwtSession: {
      recoverSession,
      requireSession: () => ({ id: 7, perfil: 'professor' }),
      authenticatedFetch: async (_base, requestPath, options = {}) => {
        calls.push(requestPath);
        requests.push({ path: requestPath, options });
        if (requestPath === '/curso' && options.method === 'POST') {
          return response({ id: 99, professorId: 7, status: 'EM_AVALIACAO' }, 201);
        }
        const configured = responses[requestPath];
        return typeof configured === 'function' ? configured(options) : configured || response([]);
      },
      logout() {}
    }
  };

  return {
    activities,
    avatar,
    calls,
    charts,
    courseFeedback,
    courseForm,
    courseName,
    courseSubmit,
    courseSubscription,
    courseType,
    name,
    requests,
    start: execute('professor/professor.js', document, window),
    values
  };
}

test('dashboard do professor apresenta KPIs e cursos reais do proprio professor', async () => {
  const harness = createProfessorDashboardHarness();

  await harness.start();

  assert.equal(harness.name.textContent, 'Professora Real');
  assert.deepEqual(harness.values.map(value => value.textContent), [1, 0, 1, 1]);
  assert.equal(harness.values.every(value => !value.attributes.has('aria-hidden')), true);
  assert.match(treeText(harness.activities), /Curso Próprio/);
  assert.doesNotMatch(treeText(harness.activities), /Curso de Outro Professor/);
  assert.match(treeText(harness.charts), /Matrículas reais/);
});

test('dashboard do professor cobre loading, listas vazias e falhas', async () => {
  const pendingCourses = deferred();
  const pending = createProfessorDashboardHarness({ responses: {
    '/curso/professor/7': pendingCourses.promise
  } });
  const loading = pending.start();
  await new Promise(resolve => setImmediate(resolve));
  assert.equal(pending.values[0].attributes.get('aria-hidden'), 'true');
  pendingCourses.resolve(response([]));
  await loading;
  assert.deepEqual(pending.values.map(value => value.textContent), [0, 0, 0, 0]);
  assert.match(treeText(pending.activities), /Nenhum curso cadastrado/);

  const invalidPayload = createProfessorDashboardHarness({ responses: {
    '/curso/professor/7': response({ id: 1 })
  } });
  await invalidPayload.start();
  assert.deepEqual(invalidPayload.values.map(value => value.textContent), ['--', '--', '--', '--']);
  assert.match(treeText(invalidPayload.activities), /Resposta de cursos inválida/);

  const sessionError = createProfessorDashboardHarness({
    recoverSession: async () => { throw new Error('Sessão indisponível'); }
  });
  await sessionError.start();
  assert.deepEqual(sessionError.calls, []);
  assert.match(treeText(sessionError.activities), /Sessão indisponível/);
  assert.equal(sessionError.values.every(value => !value.attributes.has('aria-hidden')), true);
});

test('dashboard do professor renderiza nome malicioso de curso como texto', async () => {
  const maliciousName = '<img src=x onerror=alert(1)>';
  const harness = createProfessorDashboardHarness({ responses: {
    '/curso/professor/7': response([{ id: 1, professorId: 7, nome: maliciousName, status: 'APROVADO' }]),
    '/matricula/curso/1': response([])
  } });
  await harness.start();
  assert.match(treeText(harness.activities), /<img src=x onerror=alert\(1\)>/);
  assert.equal(treeTags(harness.activities).includes('IMG'), false);
});

test('cadastro de curso aguarda a sessao e envia o professor autenticado', async () => {
  const session = deferred();
  const harness = createProfessorDashboardHarness({ recoverSession: () => session.promise });
  const loading = harness.start();

  await harness.courseForm.dispatch('submit', { preventDefault() {} });
  assert.equal(harness.courseSubmit.disabled, true);
  assert.match(harness.courseFeedback.textContent, /Aguarde o carregamento/);
  assert.equal(harness.requests.some(request => request.options.method === 'POST'), false);

  session.resolve({ id: 7, perfil: 'professor', nome: 'Professora Real' });
  await loading;
  assert.equal(harness.courseSubmit.disabled, false);
  assert.equal(harness.courseSubmit.textContent, 'Enviar para avaliacao');

  harness.courseName.value = 'Curso de Integração';
  harness.courseSubscription.value = 'PREMIUM';
  harness.courseType.value = 'ASSINCRONO';
  await harness.courseForm.dispatch('submit', { preventDefault() {} });

  const post = harness.requests.find(request => request.path === '/curso' && request.options.method === 'POST');
  assert.ok(post);
  assert.deepEqual(JSON.parse(post.options.body), {
    nome: 'Curso de Integração',
    tipoAssinatura: 'PREMIUM',
    tipoCurso: 'ASSINCRONO',
    professorId: 7
  });
  assert.match(harness.courseFeedback.textContent, /enviado para avaliacao/);
});

test('curso salvo nao e apresentado como falha quando a atualizacao do dashboard falha', async () => {
  let courseQueries = 0;
  const harness = createProfessorDashboardHarness({ responses: {
    '/curso/professor/7': () => {
      courseQueries += 1;
      return courseQueries === 1
        ? response([{ id: 1, professorId: 7, nome: 'Curso existente', status: 'APROVADO' }])
        : response({ erro: 'Falha ao atualizar dashboard' }, 500);
    },
    '/matricula/curso/1': response([])
  } });
  await harness.start();

  harness.courseName.value = 'Curso salvo';
  harness.courseSubscription.value = 'COMUM';
  harness.courseType.value = 'SINCRONO';
  harness.requests.length = 0;
  harness.courseFeedback.textContent = '';

  await harness.courseForm.dispatch('submit', { preventDefault() {} });

  assert.equal(harness.requests.filter(request => request.options.method === 'POST').length, 1);
  assert.equal(harness.courseSubmit.disabled, false);
  assert.match(harness.courseFeedback.textContent, /Curso enviado para avaliacao/);
  assert.match(harness.courseFeedback.textContent, /recarregue a pagina/);
});
