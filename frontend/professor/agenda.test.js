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

function fixedDate(now) {
  const timestamp = new Date(now).getTime();
  return class extends Date {
    constructor(...args) {
      super(...(args.length ? args : [timestamp]));
    }

    static now() {
      return timestamp;
    }
  };
}

function expectedMonth(now, offset = 0) {
  const date = new Date(now);
  date.setDate(1);
  date.setMonth(date.getMonth() + offset);
  const label = date.toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' });
  return label.charAt(0).toUpperCase() + label.slice(1);
}

function createHarness(courseResponse = response([]), now = '2026-08-18T12:00:00Z', recoverSession) {
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
      recoverSession: recoverSession || (async () => ({ id: 7, perfil: 'professor', nome: 'Professora Real' })),
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
    Date: fixedDate(now),
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

test('renderiza o mes atual enquanto mantem loading apenas nos cursos do professor', async () => {
  const courses = deferred();
  const harness = createHarness(courses.promise);

  const loading = harness.start();
  await new Promise(resolve => setImmediate(resolve));

  assert.equal(harness.monthTitle.textContent, expectedMonth('2026-08-18T12:00:00Z'));
  assert.equal(harness.calendarSection.attributes.has('aria-busy'), false);
  assert.equal(harness.eventsSection.attributes.get('aria-busy'), 'true');
  assert.equal(harness.nextButton.disabled, false);

  courses.resolve(response([
    { id: 10, professorId: 7, nome: 'Curso Real', status: 'APROVADO' },
    { id: 11, professorId: 99, nome: 'Curso de Outro Professor', status: 'APROVADO' }
  ]));
  await loading;

  assert.equal(harness.monthTitle.textContent, expectedMonth('2026-08-18T12:00:00Z'));
  assert.ok([28, 35, 42].includes(harness.calendarGrid.children.length));
  assert.equal(harness.calendarGrid.children.filter(day => day.className.includes('today')).length, 1);
  assert.match(treeText(harness.eventsList), /Curso Real/);
  assert.doesNotMatch(treeText(harness.eventsList), /Curso de Outro Professor/);
  assert.deepEqual(harness.calls, ['/curso/professor/7']);
  assert.equal(harness.calendarSection.attributes.has('aria-busy'), false);
  assert.equal(harness.eventsSection.attributes.has('aria-busy'), false);
});

test('navegacao atravessa a virada do ano e recalcula os dias', async () => {
  const now = '2026-12-15T12:00:00Z';
  const harness = createHarness(response([]), now);
  await harness.start();

  harness.navigate(harness.nextButton);
  assert.equal(harness.monthTitle.textContent, expectedMonth(now, 1));
  assert.ok([28, 35, 42].includes(harness.calendarGrid.children.length));

  harness.navigate(harness.previousButton);
  assert.equal(harness.monthTitle.textContent, expectedMonth(now));
});

test('fevereiro bissexto inclui o dia 29 no calendario', async () => {
  const harness = createHarness(response([]), '2024-02-15T12:00:00Z');
  await harness.start();

  const leapDay = harness.calendarGrid.children.find(day => day.dataset.date === '2024-02-29');
  assert.ok(leapDay);
  assert.equal(leapDay.className.includes('other-month'), false);
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

  assert.equal(harness.monthTitle.textContent, expectedMonth('2026-08-18T12:00:00Z'));
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

test('falha ao recuperar a sessao encerra o loading da agenda sem consultar cursos', async () => {
  const harness = createHarness(response([]), '2026-08-18T12:00:00Z', async () => {
    throw new Error('Servidor de sessão indisponível');
  });

  await harness.start();

  assert.deepEqual(harness.calls, []);
  assert.match(treeText(harness.eventsList), /Servidor de sessão indisponível/);
  assert.equal(harness.eventsSection.attributes.has('aria-busy'), false);
});
