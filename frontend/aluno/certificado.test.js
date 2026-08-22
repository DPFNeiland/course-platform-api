const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const source = fs.readFileSync(path.join(__dirname, 'aluno.js'), 'utf8');
const uiSource = fs.readFileSync(path.join(__dirname, '..', 'dashboard-ui.js'), 'utf8');

function response(body, status = 200) {
  return { ok: status >= 200 && status < 300, status, json: async () => body };
}

function deferred() {
  let resolve;
  const promise = new Promise(resolvePromise => { resolve = resolvePromise; });
  return { promise, resolve };
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
    matches() { return false; },
    closest() { return null; }
  };
  node.classList = {
    remove(className) {
      node.className = node.className
        .split(/\s+/)
        .filter(current => current && current !== className)
        .join(' ');
    }
  };
  return node;
}

function createHarness({
  courseResponse = response([{ id: 10, nome: 'Curso Real', status: 'APROVADO', professorId: 3 }]),
  enrollmentResponse = response([{ id: 20, alunoId: 7, cursoId: 10, status: 'ENCERRADA', dataMatricula: '2026-08-20' }]),
  detail = false,
  studentName = 'Ana Real',
  locationSearch
} = {}) {
  const listeners = {};
  const avatar = element('div');
  avatar.className = 'avatar loading-skeleton loading-skeleton-avatar';
  avatar.attributes.set('aria-label', 'Carregando perfil');
  const statsSection = element('section');
  statsSection.attributes.set('aria-busy', 'true');
  const totalCursos = element('div');
  const totalHoras = element('div');
  const mediaGeral = element('div');
  for (const stat of [totalCursos, totalHoras, mediaGeral]) {
    stat.className = 'stat-number loading-skeleton loading-skeleton-text';
    stat.attributes.set('aria-hidden', 'true');
  }
  const certificates = element('div', 'Carregando certificados...');
  const certificateName = element('div');
  const certificateCourse = element('div');
  const certificateHours = element('span');
  const certificateDate = element('span');
  const certificatePreview = element('div');
  certificatePreview.append(certificateName, certificateCourse, certificateHours, certificateDate);
  const modal = element('div');
  modal.style = {};
  const modalBody = element('div', 'Texto inicial');
  const calls = [];

  const selectors = {
    '#statsSection': detail ? null : statsSection,
    '#totalCursos': detail ? null : totalCursos,
    '#totalHoras': detail ? null : totalHoras,
    '#mediaGeral': detail ? null : mediaGeral,
    '#certificadosContainer': detail ? null : certificates,
    '#certificadoNome': detail ? certificateName : null,
    '#certificadoCurso': detail ? certificateCourse : null,
    '#certificadoCargaHoraria': detail ? certificateHours : null,
    '#certificadoData': detail ? certificateDate : null,
    '.certificado-preview': detail ? certificatePreview : null,
    '#modalCompartilhar': modal,
    '#modalCompartilharBody': modalBody
  };
  const document = {
    addEventListener(type, listener) {
      listeners[type] = listeners[type] || [];
      listeners[type].push(listener);
    },
    querySelector(selector) { return selectors[selector] || null; },
    querySelectorAll(selector) {
      return {
        '.avatar': [avatar],
        '.certificates-grid, .certificados-grid': detail ? [] : [certificates],
        '[data-approved-courses]': [],
        '[data-approved-courses], [data-student-progress]': [],
        '[data-field="nomeAluno"]': []
      }[selector] || [];
    },
    createElement: tagName => element(tagName),
    createTextNode: text => element('#text', text)
  };
  const window = {
    APP_CONFIG: { API_BASE_URL: 'https://api.test' },
    location: { href: '', search: detail ? (locationSearch ?? '?cursoId=10') : '' },
    jwtSession: {
      recoverSession: async () => ({ id: 7, perfil: 'aluno', nome: studentName }),
      requireSession: () => ({ id: 7, perfil: 'aluno', nome: studentName }),
      authenticatedFetch: async (_base, requestPath) => {
        calls.push(requestPath);
        return requestPath === '/curso' ? courseResponse : enrollmentResponse;
      },
      logout() {}
    },
    open() {}
  };
  const context = { window, document, URLSearchParams, alert() {}, console };
  vm.runInNewContext(uiSource, context);
  vm.runInNewContext(source, context);

  return {
    avatar,
    calls,
    certificateCourse,
    certificateDate,
    certificateHours,
    certificateName,
    certificatePreview,
    certificates,
    click: event => listeners.click[0](event),
    mediaGeral,
    modal,
    modalBody,
    start: () => listeners.DOMContentLoaded[0](),
    statsSection,
    totalCursos,
    totalHoras
  };
}

function treeTags(node) {
  return [node.tagName, ...(node.children || []).flatMap(treeTags)].filter(Boolean);
}

function treeText(node) {
  return [node.textContent, ...(node.children || []).map(treeText)].join(' ');
}

test('mantém stats como skeleton até concluir a consulta e então exibe apenas dados reais', async () => {
  const enrollments = deferred();
  const harness = createHarness({ enrollmentResponse: enrollments.promise });

  const loading = harness.start();
  await new Promise(resolve => setImmediate(resolve));

  assert.equal(harness.statsSection.attributes.get('aria-busy'), 'true');
  assert.match(harness.totalCursos.className, /loading-skeleton/);
  assert.equal(harness.totalCursos.textContent, '');

  enrollments.resolve(response([{ id: 20, alunoId: 7, cursoId: 10, status: 'ENCERRADA' }]));
  await loading;

  assert.equal(harness.avatar.textContent, 'AR');
  assert.doesNotMatch(harness.avatar.className, /loading-skeleton/);
  assert.equal(harness.totalCursos.textContent, '1');
  assert.equal(harness.totalHoras.textContent, 'Não disponível');
  assert.equal(harness.mediaGeral.textContent, 'Não disponível');
  assert.equal(harness.statsSection.attributes.has('aria-busy'), false);
});

test('gera iniciais com primeiro e último nome e preserva fallbacks', async () => {
  const cases = [
    ['Ana Real', 'AR'],
    ['João da Silva', 'JS'],
    ['Maria', 'MA'],
    ['  Maria   dos   Santos  ', 'MS'],
    ['', '?'],
    [null, '?']
  ];

  for (const [studentName, expected] of cases) {
    const harness = createHarness({ studentName });
    await harness.start();
    assert.equal(harness.avatar.textContent, expected);
  }
});

test('resposta vazia apresenta zero somente depois de encerrar o skeleton', async () => {
  const enrollments = deferred();
  const harness = createHarness({ enrollmentResponse: enrollments.promise });

  const loading = harness.start();
  await new Promise(resolve => setImmediate(resolve));

  assert.equal(harness.totalCursos.textContent, '');
  assert.match(harness.totalCursos.className, /loading-skeleton/);

  enrollments.resolve(response([]));
  await loading;

  assert.equal(harness.totalCursos.textContent, '0');
  assert.equal(harness.totalHoras.textContent, 'Não disponível');
  assert.equal(harness.mediaGeral.textContent, 'Não disponível');
  assert.match(treeText(harness.certificates), /Nenhum certificado disponível/);
  assert.equal(harness.statsSection.attributes.has('aria-busy'), false);
});

test('falha da API de matrículas encerra skeleton sem apresentar zero como dado real', async () => {
  const harness = createHarness({ enrollmentResponse: response({ message: 'API indisponível' }, 500) });

  await harness.start();

  assert.equal(harness.totalCursos.textContent, 'Indisponível');
  assert.equal(harness.totalHoras.textContent, 'Indisponível');
  assert.equal(harness.mediaGeral.textContent, 'Indisponível');
  assert.equal(harness.statsSection.attributes.has('aria-busy'), false);
  assert.equal(harness.certificates.children[0].textContent, 'API indisponível');
});

test('falha da API de cursos preserva o total calculado pelas matrículas', async () => {
  const harness = createHarness({ courseResponse: response({ message: 'API indisponível' }, 500) });

  await harness.start();

  assert.equal(harness.totalCursos.textContent, '1');
  assert.equal(harness.totalHoras.textContent, 'Não disponível');
  assert.equal(harness.mediaGeral.textContent, 'Não disponível');
  assert.match(treeText(harness.certificates), /Curso #10/);
  assert.equal(harness.statsSection.attributes.has('aria-busy'), false);
});

test('falha de rede em cursos preserva matrículas e encerra o loading', async () => {
  const harness = createHarness({ courseResponse: Promise.reject(new Error('Falha de rede')) });

  await harness.start();

  assert.equal(harness.totalCursos.textContent, '1');
  assert.match(treeText(harness.certificates), /Curso #10/);
  assert.equal(harness.statsSection.attributes.has('aria-busy'), false);
});

test('payload de cursos fora do contrato não apaga os certificados reais', async () => {
  const harness = createHarness({ courseResponse: response({ id: 10, nome: 'Formato inválido' }) });

  await harness.start();

  assert.equal(harness.totalCursos.textContent, '1');
  assert.match(treeText(harness.certificates), /Curso #10/);
  assert.equal(harness.statsSection.attributes.has('aria-busy'), false);
});

test('detalhe usa mensagem amigável quando o backend não informa carga horária', async () => {
  const harness = createHarness({ detail: true });

  await harness.start();

  assert.equal(harness.avatar.textContent, 'AR');
  assert.equal(harness.certificateName.textContent, 'Ana Real');
  assert.equal(harness.certificateCourse.textContent, 'Curso Real');
  assert.equal(harness.certificateHours.textContent, 'Carga horária não disponível para este curso.');
  assert.equal(harness.certificateDate.textContent, 'Data de emissão não disponível');
  assert.doesNotMatch(harness.certificateHours.className, /loading-skeleton/);
});

test('falha ao complementar dados do curso não invalida matrícula encerrada', async () => {
  const harness = createHarness({
    detail: true,
    courseResponse: Promise.reject(new Error('Falha de rede'))
  });

  await harness.start();

  assert.equal(harness.certificateName.textContent, 'Ana Real');
  assert.equal(harness.certificateCourse.textContent, 'Dados do curso indisponíveis');
  assert.doesNotMatch(treeText(harness.certificatePreview), /Certificado não encontrado/);
});

test('usa campos próprios de conclusão e emissão quando fornecidos pelo backend', async () => {
  const enrollment = {
    id: 20,
    alunoId: 7,
    cursoId: 10,
    status: 'ENCERRADA',
    dataMatricula: '2026-01-10',
    dataConclusao: '2026-08-19',
    dataEmissao: '2026-08-20'
  };
  const listHarness = createHarness({ enrollmentResponse: response([enrollment]) });
  await listHarness.start();
  assert.match(treeText(listHarness.certificates), /2026-08-19/);
  assert.doesNotMatch(treeText(listHarness.certificates), /2026-01-10/);

  const detailHarness = createHarness({ detail: true, enrollmentResponse: response([enrollment]) });
  await detailHarness.start();
  assert.equal(detailHarness.certificateDate.textContent, '2026-08-20');
});

test('URL ausente ou inválida nunca seleciona outro certificado concluído', async () => {
  const invalidSearches = ['', '?cursoId=', '?cursoId=0', '?cursoId=abc', '?cursoId=999'];

  for (const locationSearch of invalidSearches) {
    const harness = createHarness({ detail: true, locationSearch });
    await harness.start();

    assert.match(treeText(harness.certificatePreview), /Certificado não encontrado/);
    assert.doesNotMatch(treeText(harness.certificatePreview), /Curso Real/);
  }
});

test('curso sem matrícula encerrada não gera certificado', async () => {
  const harness = createHarness({
    detail: true,
    enrollmentResponse: response([{ id: 20, alunoId: 7, cursoId: 10, status: 'ATIVA' }])
  });

  await harness.start();

  assert.match(treeText(harness.certificatePreview), /Certificado não encontrado/);
  assert.doesNotMatch(treeText(harness.certificatePreview), /Curso Real/);
});

test('modal descreve o fluxo real e trata nome do curso somente como texto', async () => {
  const maliciousName = '<img src=x onerror=alert(1)>';
  const harness = createHarness({
    courseResponse: response([{ id: 10, nome: maliciousName, status: 'APROVADO', professorId: 3 }])
  });
  await harness.start();

  const shareButton = {
    closest(selector) {
      return selector === '[data-share-certificate]' ? { dataset: { shareCertificate: '10' } } : null;
    },
    matches() { return false; }
  };
  harness.click({ target: shareButton });

  assert.match(harness.modalBody.textContent, /LinkedIn será aberto em uma nova aba/);
  assert.match(harness.modalBody.textContent, /<img src=x onerror=alert\(1\)>/);
  assert.doesNotMatch(harness.modalBody.textContent, /<strong>/);
  assert.equal(treeTags(harness.certificates).includes('IMG'), false);
  assert.equal(harness.modal.style.display, 'block');
});
