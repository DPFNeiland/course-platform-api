const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const uiSource = fs.readFileSync(path.join(__dirname, 'dashboard-ui.js'), 'utf8');

function response(body, status = 200) {
  return { ok: status >= 200 && status < 300, status, json: async () => body };
}

function element(tagName, textContent = '') {
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
    addEventListener() {},
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

test('dashboard do aluno encerra skeleton com dados reais e sem pontuacao provisoria', async () => {
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

  const document = {
    querySelector(selector) {
      return {
        '[data-dashboard-gamification]': gamification,
        '[data-dashboard-course-list]': courseList,
        '.gamificacao': gamificationCard
      }[selector] || null;
    },
    querySelectorAll(selector) {
      return {
        '[data-approved-courses]': [],
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
      recoverSession: async () => ({ id: 7, perfil: 'aluno', nome: 'Aluna Real' }),
      requireSession: () => ({ id: 7, perfil: 'aluno' }),
      authenticatedFetch: async (_base, requestPath) => requestPath === '/curso'
        ? response([{ id: 10, nome: 'Curso Real', professorId: 3, status: 'APROVADO' }])
        : response([{ id: 20, alunoId: 7, cursoId: 10, status: 'ATIVA' }]),
      logout() {}
    },
    open() {}
  };

  await execute('aluno/aluno.js', document, window)();

  assert.equal(name.textContent, 'Aluna Real');
  assert.equal(gamification.textContent, 'Gamificação em breve.');
  assert.equal(gamification.attributes.has('aria-hidden'), false);
  assert.doesNotMatch(treeText(gamificationCard), /pontos/i);
  assert.match(treeText(courseList), /Curso Real/);
  assert.equal(courseCard.attributes.has('aria-busy'), false);
});

test('dashboard do curador apresenta identidade e contagens reais', async () => {
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
      recoverSession: async () => ({ id: 2, perfil: 'curador', nome: 'Curadora Real' }),
      requireSession: () => ({ perfil: 'curador' }),
      authenticatedFetch: async (_base, requestPath) => ({
        '/curso': response([{ id: 1, status: 'EM_AVALIACAO' }]),
        '/matricula': response([{ id: 1 }, { id: 2 }])
      }[requestPath] || response([])),
      logout() {}
    }
  };

  await execute('curador/curador.js', document, window)();

  assert.equal(name.textContent, 'Curadora Real');
  assert.equal(avatar.textContent, 'CU');
  assert.match(pendingSummary.textContent, /^1 registro/);
  assert.match(enrollmentSummary.textContent, /^2 registro/);
  assert.equal(pendingSummary.attributes.has('aria-hidden'), false);
});

test('dashboard do professor apresenta KPIs e cursos reais do proprio professor', async () => {
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
    getElementById(id) { return id === 'courseForm' ? courseForm : null; },
    createElement: tagName => element(tagName)
  };
  const window = {
    APP_CONFIG: { API_BASE_URL: 'https://api.test' },
    location: { href: '' },
    jwtSession: {
      recoverSession: async () => ({ id: 7, perfil: 'professor', nome: 'Professora Real' }),
      requireSession: () => ({ id: 7, perfil: 'professor' }),
      authenticatedFetch: async (_base, requestPath) => ({
        '/curso': response([
          { id: 1, professorId: 7, nome: 'Curso Próprio', status: 'APROVADO' },
          { id: 2, professorId: 99, nome: 'Curso de Outro Professor', status: 'APROVADO' }
        ]),
        '/matricula': response([
          { id: 1, cursoId: 1 },
          { id: 2, cursoId: 2 }
        ])
      }[requestPath] || response([])),
      logout() {}
    }
  };

  await execute('professor/professor.js', document, window)();

  assert.equal(name.textContent, 'Professora Real');
  assert.deepEqual(values.map(value => value.textContent), [1, 0, 1, 1]);
  assert.equal(values.every(value => !value.attributes.has('aria-hidden')), true);
  assert.match(treeText(activities), /Curso Próprio/);
  assert.doesNotMatch(treeText(activities), /Curso de Outro Professor/);
  assert.match(treeText(charts), /Matrículas reais/);
});
