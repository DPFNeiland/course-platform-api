const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const source = fs.readFileSync(path.join(__dirname, 'professor.js'), 'utf8');

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
    disabled: false,
    children: [],
    attributes: new Map(),
    appendChild(child) {
      this.children.push(child);
      return child;
    },
    append(...children) {
      this.children.push(...children);
    },
    replaceChildren(...children) {
      this.children = children;
    },
    setAttribute(name, value) {
      this.attributes.set(name, String(value));
    },
    removeAttribute(name) {
      this.attributes.delete(name);
    },
    closest(selector) {
      return selector === '[data-month-direction]' && this.dataset.monthDirection ? this : null;
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

function expectedMonth(offset = 0) {
  const date = new Date();
  date.setDate(1);
  date.setMonth(date.getMonth() + offset);
  const label = date.toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' });
  return label.charAt(0).toUpperCase() + label.slice(1);
}

function createHarness(courseResponse = response([])) {
  const listeners = {};
  const calls = [];
  const calendarSection = element('section');
  calendarSection.attributes.set('aria-busy', 'true');
  const eventsSection = element('section');
  eventsSection.attributes.set('aria-busy', 'true');
  const calendarGrid = element('div', 'Carregando calendário atual...');
  const eventsList = element('div', 'Carregando cursos reais...');
  const monthTitle = element('h2', 'Carregando mês atual...');
  const previousButton = element('button');
  previousButton.dataset.monthDirection = '-1';
  previousButton.disabled = true;
  const nextButton = element('button');
  nextButton.dataset.monthDirection = '1';
  nextButton.disabled = true;

  const document = {
    addEventListener(type, listener) {
      listeners[type] = listeners[type] || [];
      listeners[type].push(listener);
    },
    querySelector(selector) {
      const elements = {
        '.calendar-grid': calendarGrid,
        '.calendar-section': calendarSection,
        '.events-list': eventsList,
        '.events-section': eventsSection,
        '.month-title': monthTitle
      };
      return elements[selector] || null;
    },
    querySelectorAll(selector) {
      if (selector === '[data-month-direction]') return [previousButton, nextButton];
      return [];
    },
    getElementById() {
      return null;
    },
    createElement(tagName) {
      return element(tagName);
    }
  };
  const window = {
    APP_CONFIG: { API_BASE_URL: 'https://api.test' },
    location: { href: '' },
    jwtSession: {
      recoverSession: async () => ({ id: 7, perfil: 'professor', nome: 'Professora Real' }),
      requireSession: () => ({ id: 7, perfil: 'professor' }),
      authenticatedFetch: async (_baseUrl, requestPath) => {
        calls.push(requestPath);
        return courseResponse instanceof Promise ? courseResponse : courseResponse;
      },
      logout() {}
    }
  };
  const context = {
    window,
    document,
    sessionStorage: { setItem() {} },
    alert() {},
    console
  };

  vm.runInNewContext(source, context);

  return {
    calendarGrid,
    calendarSection,
    calls,
    eventsList,
    eventsSection,
    monthTitle,
    nextButton,
    previousButton,
    start: () => listeners.DOMContentLoaded[0](),
    navigate: button => listeners.click[0]({ target: button })
  };
}

test('mantem loading ate os cursos chegarem e renderiza o mes atual com dados do professor', async () => {
  const courses = deferred();
  const harness = createHarness(courses.promise);

  const loading = harness.start();
  await new Promise(resolve => setImmediate(resolve));

  assert.equal(harness.monthTitle.textContent, 'Carregando mês atual...');
  assert.equal(harness.calendarSection.attributes.get('aria-busy'), 'true');
  assert.equal(harness.nextButton.disabled, true);

  courses.resolve(response([
    { id: 10, professorId: 7, nome: 'Curso Real', status: 'APROVADO' },
    { id: 11, professorId: 99, nome: 'Curso de Outro Professor', status: 'APROVADO' }
  ]));
  await loading;

  assert.equal(harness.monthTitle.textContent, expectedMonth());
  assert.ok([28, 35, 42].includes(harness.calendarGrid.children.length));
  assert.equal(harness.calendarGrid.children.filter(day => day.className.includes('today')).length, 1);
  assert.match(treeText(harness.eventsList), /Curso Real/);
  assert.doesNotMatch(treeText(harness.eventsList), /Curso de Outro Professor/);
  assert.deepEqual(harness.calls, ['/curso']);
  assert.equal(harness.calendarSection.attributes.has('aria-busy'), false);
  assert.equal(harness.eventsSection.attributes.has('aria-busy'), false);
});

test('navegacao altera o mes exibido e recalcula os dias', async () => {
  const harness = createHarness();
  await harness.start();

  harness.navigate(harness.nextButton);
  assert.equal(harness.monthTitle.textContent, expectedMonth(1));
  assert.ok([28, 35, 42].includes(harness.calendarGrid.children.length));

  harness.navigate(harness.previousButton);
  assert.equal(harness.monthTitle.textContent, expectedMonth());
});

test('lista vazia apresenta estado real sem inventar eventos', async () => {
  const harness = createHarness();
  await harness.start();

  assert.match(treeText(harness.eventsList), /Nenhum curso vinculado ao seu perfil/);
  assert.doesNotMatch(treeText(harness.eventsList), /Reunião|Atendimento|Avaliação/);
});

test('erro ao carregar cursos encerra o loading e preserva o calendario atual', async () => {
  const harness = createHarness(response({ message: 'API indisponível' }, 500));
  await harness.start();

  assert.equal(harness.monthTitle.textContent, expectedMonth());
  assert.match(treeText(harness.eventsList), /API indisponível/);
  assert.equal(harness.calendarSection.attributes.has('aria-busy'), false);
  assert.equal(harness.eventsSection.attributes.has('aria-busy'), false);
});

test('payload fora do contrato e tratado como erro sem manter placeholders infinitos', async () => {
  const harness = createHarness(response({ id: 10, nome: 'Formato inválido' }));
  await harness.start();

  assert.match(treeText(harness.eventsList), /Resposta de cursos inválida/);
  assert.equal(harness.eventsSection.attributes.has('aria-busy'), false);
});

test('nome de curso recebido da API e exibido como texto sem criar HTML', async () => {
  const harness = createHarness(response([{
    id: 10,
    professorId: 7,
    nome: '<script>alert(1)</script>',
    status: 'APROVADO'
  }]));
  await harness.start();

  assert.match(treeText(harness.eventsList), /<script>alert\(1\)<\/script>/);
  assert.equal(treeTags(harness.eventsList).includes('SCRIPT'), false);
});
