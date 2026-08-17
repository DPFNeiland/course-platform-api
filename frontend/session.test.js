const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const source = fs.readFileSync(path.join(__dirname, 'session.js'), 'utf8');

function loadSession(initial = {}) {
  const storage = new Map(Object.entries(initial));
  const sessionStorage = {
    getItem: key => storage.has(key) ? storage.get(key) : null,
    setItem: (key, value) => storage.set(key, String(value)),
    removeItem: key => storage.delete(key)
  };
  const window = { location: { href: '' } };
  const context = { window, sessionStorage, Date, encodeURIComponent };
  vm.runInNewContext(source, context);
  return { storage, window, jwtSession: window.jwtSession };
}

function validSession(overrides = {}) {
  return {
    token: 'jwt-valido',
    perfil: 'aluno',
    expiraEm: new Date(Date.now() + 60_000).toISOString(),
    ...overrides
  };
}

test('remove a chave legada user ao inicializar', () => {
  const loaded = loadSession({ user: JSON.stringify({ token: 'base64-antigo' }) });
  assert.equal(loaded.storage.has('user'), false);
});

test('caminho feliz retorna a sessao valida com perfil normalizado', () => {
  const session = validSession({ perfil: ' ALUNO ' });
  const loaded = loadSession({ session: JSON.stringify(session) });

  const result = loaded.jwtSession.requireSession(['aluno'], '../index.html');

  assert.equal(result.token, 'jwt-valido');
  assert.equal(result.perfil, 'aluno');
  assert.equal(loaded.storage.has('session'), true);
  assert.equal(loaded.window.location.href, '');
});

test('sessao sem token e considerada invalida', () => {
  const loaded = loadSession({
    session: JSON.stringify(validSession({ token: '' }))
  });

  assert.equal(loaded.jwtSession.requireSession(['aluno'], '../index.html'), null);
  assert.equal(loaded.storage.has('session'), false);
  assert.equal(loaded.window.location.href, '../index.html?auth=expired');
});

test('sessao corrompida e limpa sem propagar erro de JSON', () => {
  const loaded = loadSession({ session: '{json-invalido' });

  assert.doesNotThrow(() => loaded.jwtSession.requireSession(['aluno'], '../index.html'));
  assert.equal(loaded.storage.has('session'), false);
  assert.equal(loaded.window.location.href, '../index.html?auth=expired');
});

test('perfil sem permissao limpa a sessao e informa bloqueio', () => {
  const loaded = loadSession({ session: JSON.stringify(validSession()) });

  assert.equal(loaded.jwtSession.requireSession(['professor'], '../index.html'), null);
  assert.equal(loaded.storage.has('session'), false);
  assert.equal(loaded.window.location.href, '../index.html?auth=forbidden');
});

test('sessao expirada limpa dados e redireciona para login', () => {
  const expired = validSession({ expiraEm: new Date(Date.now() - 60_000).toISOString() });
  const loaded = loadSession({ session: JSON.stringify(expired), user: 'legado' });

  assert.equal(loaded.jwtSession.requireSession(['aluno'], '../index.html'), null);
  assert.equal(loaded.storage.has('session'), false);
  assert.equal(loaded.storage.has('user'), false);
  assert.equal(loaded.window.location.href, '../index.html?auth=expired');
});

test('TOKEN_EXPIRED limpa a sessao e informa expiracao', async () => {
  const loaded = loadSession({ session: JSON.stringify(validSession()), user: 'legado' });
  const handled = await loaded.jwtSession.handleUnauthorized({
    status: 401,
    json: async () => ({ codigo: 'TOKEN_EXPIRED' })
  }, '../index.html');

  assert.equal(handled, true);
  assert.equal(loaded.storage.has('session'), false);
  assert.equal(loaded.storage.has('user'), false);
  assert.equal(loaded.window.location.href, '../index.html?auth=expired');
});

test('token invalido limpa a sessao e informa invalidade', async () => {
  const loaded = loadSession({ session: JSON.stringify(validSession()) });
  await loaded.jwtSession.handleUnauthorized({
    status: 401,
    json: async () => ({ codigo: 'TOKEN_INVALID' })
  }, '../index.html');

  assert.equal(loaded.storage.has('session'), false);
  assert.equal(loaded.window.location.href, '../index.html?auth=invalid');
});

test('resposta diferente de 401 preserva a sessao', async () => {
  const original = JSON.stringify(validSession());
  const loaded = loadSession({ session: original });
  const handled = await loaded.jwtSession.handleUnauthorized({ status: 403 }, '../index.html');

  assert.equal(handled, false);
  assert.equal(loaded.storage.get('session'), original);
  assert.equal(loaded.window.location.href, '');
});

test('logout limpa sessao e chave legada', () => {
  const loaded = loadSession({ session: JSON.stringify(validSession()), user: 'legado' });
  loaded.jwtSession.logout('../index.html');

  assert.equal(loaded.storage.has('session'), false);
  assert.equal(loaded.storage.has('user'), false);
  assert.equal(loaded.window.location.href, '../index.html?auth=logout');
});
