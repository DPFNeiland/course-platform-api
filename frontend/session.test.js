const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const source = fs.readFileSync(path.join(__dirname, 'session.js'), 'utf8');

function loadSession(initial = {}, fetchImpl = async () => ({ status: 204 })) {
  const storage = new Map(Object.entries(initial));
  const sessionStorage = {
    getItem: key => storage.has(key) ? storage.get(key) : null,
    setItem: (key, value) => storage.set(key, String(value)),
    removeItem: key => storage.delete(key)
  };
  const window = { location: { href: '' } };
  const context = { window, sessionStorage, Date, encodeURIComponent, fetch: fetchImpl };
  vm.runInNewContext(source, context);
  return { storage, window, jwtSession: window.jwtSession };
}

function validSession(overrides = {}) {
  return {
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

  assert.equal('token' in result, false);
  assert.equal(result.perfil, 'aluno');
  assert.equal(loaded.storage.has('session'), true);
  assert.equal(loaded.window.location.href, '');
});

test('sessao visual valida nao depende do token JWT', () => {
  const loaded = loadSession({ session: JSON.stringify(validSession()) });
  const result = loaded.jwtSession.requireSession(['aluno'], '../index.html');

  assert.equal(result.perfil, 'aluno');
  assert.equal('token' in result, false);
  assert.equal(loaded.window.location.href, '');
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

test('requisicao de escrita inclui credenciais e token CSRF', async () => {
  const calls = [];
  const loaded = loadSession({}, async (url, options) => {
    calls.push({ url, options });
    if (url.endsWith('/csrf')) {
      return { ok: true, json: async () => ({ token: 'csrf-seguro' }) };
    }
    return { ok: true, status: 204 };
  });
  await loaded.jwtSession.authenticatedFetch(
    'http://localhost:8080', '/recurso',
    { method: 'POST', headers: { 'Content-Type': 'application/json' } },
    '../index.html'
  );

  assert.equal(calls[1].options.credentials, 'include');
  assert.equal(calls[1].options.headers['X-XSRF-TOKEN'], 'csrf-seguro');
  assert.equal(calls[1].options.headers['Content-Type'], 'application/json');
});

test('requisicao GET inclui cookie sem exigir CSRF', async () => {
  const calls = [];
  const loaded = loadSession({}, async (url, options) => {
    calls.push({ url, options });
    return { ok: true, status: 200 };
  });
  await loaded.jwtSession.authenticatedFetch(
    'http://localhost:8080', '/recurso', {}, '../index.html'
  );

  assert.equal(calls[0].options.credentials, 'include');
  assert.equal('X-XSRF-TOKEN' in calls[0].options.headers, false);
  assert.equal(loaded.jwtSession.authenticatedOptions, undefined);
});

test('logout invalida cookie no backend e limpa dados locais', async () => {
  const calls = [];
  const loaded = loadSession(
    { session: JSON.stringify(validSession()), user: 'legado' },
    async (url, options) => {
      calls.push({ url, options });
      if (url.endsWith('/csrf')) {
        return { ok: true, json: async () => ({ token: 'csrf-logout' }) };
      }
      return { ok: true, status: 204 };
    }
  );
  const completed = await loaded.jwtSession.logout('http://localhost:8080', '../index.html');

  assert.equal(calls.length, 2);
  assert.equal(calls[0].url, 'http://localhost:8080/csrf');
  assert.equal(calls[1].url, 'http://localhost:8080/logout');
  assert.equal(calls[1].options.method, 'POST');
  assert.equal(calls[1].options.credentials, 'include');
  assert.equal(calls[1].options.headers['X-XSRF-TOKEN'], 'csrf-logout');
  assert.equal(loaded.storage.has('session'), false);
  assert.equal(loaded.storage.has('user'), false);
  assert.equal(loaded.window.location.href, '../index.html?auth=logout');
  assert.equal(completed, true);
});

test('401 ao obter CSRF expira a sessao e redireciona', async () => {
  const loaded = loadSession(
    { session: JSON.stringify(validSession()) },
    async () => ({
      ok: false,
      status: 401,
      json: async () => ({ codigo: 'TOKEN_EXPIRED' })
    })
  );

  await assert.rejects(
    loaded.jwtSession.authenticatedFetch(
      'http://localhost:8080', '/recurso', { method: 'POST' }, '../index.html'
    ),
    /Sessao expirada/
  );
  assert.equal(loaded.storage.has('session'), false);
  assert.equal(loaded.window.location.href, '../index.html?auth=expired');
});

test('CSRF rejeitado e renovado com apenas uma repeticao', async () => {
  const calls = [];
  let csrfCount = 0;
  let writeCount = 0;
  const loaded = loadSession({}, async (url, options) => {
    calls.push({ url, options });
    if (url.endsWith('/csrf')) {
      csrfCount += 1;
      return { ok: true, status: 200, json: async () => ({ token: `csrf-${csrfCount}` }) };
    }
    writeCount += 1;
    if (writeCount === 1) {
      const body = { codigo: 'CSRF_INVALID' };
      return {
        ok: false,
        status: 403,
        clone: () => ({ json: async () => body }),
        json: async () => body
      };
    }
    return { ok: true, status: 204 };
  });

  const response = await loaded.jwtSession.authenticatedFetch(
    'http://localhost:8080', '/logout', { method: 'POST' }, '../index.html'
  );

  assert.equal(response.status, 204);
  assert.equal(csrfCount, 2);
  assert.equal(writeCount, 2);
  assert.equal(calls[1].options.headers['X-XSRF-TOKEN'], 'csrf-1');
  assert.equal(calls[3].options.headers['X-XSRF-TOKEN'], 'csrf-2');
});

test('segunda rejeicao CSRF encerra sem terceira tentativa', async () => {
  let csrfCount = 0;
  let writeCount = 0;
  const loaded = loadSession({}, async url => {
    if (url.endsWith('/csrf')) {
      csrfCount += 1;
      return { ok: true, status: 200, json: async () => ({ token: `csrf-${csrfCount}` }) };
    }
    writeCount += 1;
    const body = { codigo: 'CSRF_INVALID' };
    return {
      ok: false,
      status: 403,
      clone: () => ({ json: async () => body }),
      json: async () => body
    };
  });

  const response = await loaded.jwtSession.authenticatedFetch(
    'http://localhost:8080', '/recurso', { method: 'POST' }, '../index.html'
  );

  assert.equal(response.status, 403);
  assert.equal(csrfCount, 2);
  assert.equal(writeCount, 2);
});

test('logout expirado preserva o motivo expired', async () => {
  const loaded = loadSession(
    { session: JSON.stringify(validSession()) },
    async () => ({
      ok: false,
      status: 401,
      json: async () => ({ codigo: 'TOKEN_EXPIRED' })
    })
  );

  await loaded.jwtSession.logout('http://localhost:8080', '../index.html');

  assert.equal(loaded.storage.has('session'), false);
  assert.equal(loaded.window.location.href, '../index.html?auth=expired');
});

test('logout com token invalido preserva o motivo invalid', async () => {
  const loaded = loadSession(
    { session: JSON.stringify(validSession()) },
    async () => ({
      ok: false,
      status: 401,
      json: async () => ({ codigo: 'TOKEN_INVALID' })
    })
  );

  await loaded.jwtSession.logout('http://localhost:8080', '../index.html');

  assert.equal(loaded.storage.has('session'), false);
  assert.equal(loaded.window.location.href, '../index.html?auth=invalid');
});

test('falha de rede no logout limpa dados visuais sem informar sucesso', async () => {
  const loaded = loadSession(
    { session: JSON.stringify(validSession()), user: 'legado' },
    async () => { throw new Error('rede indisponivel'); }
  );

  const completed = await loaded.jwtSession.logout('http://localhost:8080', '../index.html');
  assert.equal(loaded.storage.has('session'), false);
  assert.equal(loaded.storage.has('user'), false);
  assert.equal(loaded.window.location.href, '../index.html?auth=logout_failed');
  assert.equal(completed, false);
});

for (const status of [403, 500]) {
  test(`logout HTTP ${status} nao e apresentado como concluido`, async () => {
    const loaded = loadSession(
      { session: JSON.stringify(validSession()) },
      async url => url.endsWith('/csrf')
        ? { ok: true, status: 200, json: async () => ({ token: 'csrf-logout' }) }
        : { ok: false, status, clone: () => ({ json: async () => ({ codigo: 'ERRO' }) }) }
    );

    const completed = await loaded.jwtSession.logout('http://localhost:8080', '../index.html');
    assert.equal(completed, false);
    assert.equal(loaded.storage.has('session'), false);
    assert.equal(loaded.window.location.href, '../index.html?auth=logout_failed');
  });
}

test('segunda rejeicao CSRF no logout nao informa sucesso nem repete novamente', async () => {
  let csrfCount = 0;
  let logoutCount = 0;
  const loaded = loadSession({ session: JSON.stringify(validSession()) }, async url => {
    if (url.endsWith('/csrf')) {
      csrfCount += 1;
      return { ok: true, status: 200, json: async () => ({ token: `csrf-${csrfCount}` }) };
    }
    logoutCount += 1;
    const body = { codigo: 'CSRF_INVALID' };
    return {
      ok: false,
      status: 403,
      clone: () => ({ json: async () => body })
    };
  });

  const completed = await loaded.jwtSession.logout('http://localhost:8080', '../index.html');
  assert.equal(completed, false);
  assert.equal(csrfCount, 2);
  assert.equal(logoutCount, 2);
  assert.equal(loaded.window.location.href, '../index.html?auth=logout_failed');
});

test('requisicao publica de autenticacao busca CSRF e inclui credenciais', async () => {
  const calls = [];
  const loaded = loadSession({}, async (url, options) => {
    calls.push({ url, options });
    if (url.endsWith('/csrf')) {
      return { ok: true, status: 200, json: async () => ({ token: 'csrf-login' }) };
    }
    return { ok: true, status: 200 };
  });

  await loaded.jwtSession.csrfFetch('http://localhost:8080', '/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: '{}'
  });

  assert.equal(calls[0].url, 'http://localhost:8080/csrf');
  assert.equal(calls[0].options.credentials, 'include');
  assert.equal(calls[1].url, 'http://localhost:8080/login');
  assert.equal(calls[1].options.credentials, 'include');
  assert.equal(calls[1].options.headers['X-XSRF-TOKEN'], 'csrf-login');
});

test('recupera sessao em nova aba sem JWT', async () => {
  const recovered = validSession({ id: 10, nome: 'Aluno Teste' });
  const loaded = loadSession({}, async url => {
    assert.equal(url, 'http://localhost:8080/session');
    return { ok: true, status: 200, json: async () => recovered };
  });

  const session = await loaded.jwtSession.recoverSession(
    'http://localhost:8080', ['aluno'], '../index.html'
  );

  assert.equal(session.id, 10);
  assert.equal('token' in session, false);
  assert.equal(JSON.parse(loaded.storage.get('session')).nome, 'Aluno Teste');
});

test('recuperacao em nova aba exige login quando o cookie expirou', async () => {
  const loaded = loadSession({}, async () => ({
    ok: false,
    status: 401,
    json: async () => ({ codigo: 'TOKEN_EXPIRED' })
  }));

  const session = await loaded.jwtSession.recoverSession(
    'http://localhost:8080', ['aluno'], '../index.html'
  );

  assert.equal(session, null);
  assert.equal(loaded.storage.has('session'), false);
  assert.equal(loaded.window.location.href, '../index.html?auth=expired');
});

test('erro do servidor ao recuperar sessao nao e tratado como token expirado', async () => {
  const loaded = loadSession({}, async () => ({ ok: false, status: 500 }));

  await assert.rejects(
    loaded.jwtSession.recoverSession(
      'http://localhost:8080', ['aluno'], '../index.html'
    ),
    /Nao foi possivel recuperar a sessao/
  );

  assert.equal(loaded.window.location.href, '');
});
