(function(global) {
  'use strict';

  const SESSION_KEY = 'session';
  const LEGACY_USER_KEY = 'user';
  const ISO_INSTANT = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d{1,9})?Z$/;
  const SAFE_METHODS = new Set(['GET', 'HEAD', 'OPTIONS']);

  // Remove credenciais Base64 que possam ter sido gravadas por versoes anteriores.
  sessionStorage.removeItem(LEGACY_USER_KEY);

  const read = () => {
    try {
      const value = sessionStorage.getItem(SESSION_KEY);
      return value ? JSON.parse(value) : null;
    } catch {
      return null;
    }
  };

  const isValid = session => {
    if (!session) return false;
    if (typeof session.expiraEm !== 'string' || !ISO_INSTANT.test(session.expiraEm)) return false;
    const expiration = Date.parse(session.expiraEm);
    return Number.isFinite(expiration) && expiration > Date.now();
  };

  const clearAndRedirect = (loginPath, reason = 'invalid') => {
    sessionStorage.removeItem(SESSION_KEY);
    sessionStorage.removeItem(LEGACY_USER_KEY);
    window.location.href = `${loginPath}?auth=${encodeURIComponent(reason)}`;
  };

  let csrfToken = null;

  const ensureCsrfToken = async (apiBaseUrl, loginPath) => {
    if (csrfToken) return csrfToken;

    const response = await fetch(`${apiBaseUrl}/csrf`, { credentials: 'include' });
    if (response.status === 401 && loginPath) {
      await handleUnauthorized(response, loginPath);
      throw new Error('Sessao expirada. Faca login novamente.');
    }
    if (!response.ok) throw new Error('Nao foi possivel iniciar uma requisicao segura.');
    const payload = await response.json().catch(() => null);
    if (!payload?.token) throw new Error('Token CSRF ausente. Recarregue a pagina.');
    csrfToken = payload.token;
    return csrfToken;
  };

  const authenticatedOptions = async (apiBaseUrl, options = {}, loginPath) => {
    const method = String(options.method || 'GET').toUpperCase();
    const headers = { ...(options.headers || {}) };
    if (!SAFE_METHODS.has(method)) {
      headers['X-XSRF-TOKEN'] = await ensureCsrfToken(apiBaseUrl, loginPath);
    }
    return { ...options, headers, credentials: 'include' };
  };

  const csrfFetch = async (apiBaseUrl, path, options = {}, loginPath, retryCsrf = true) => {
    const response = await fetch(
      `${apiBaseUrl}${path}`,
      await authenticatedOptions(apiBaseUrl, options, loginPath)
    );
    if (response.status === 401 && loginPath) {
      await handleUnauthorized(response, loginPath);
      return response;
    }
    if (response.status === 403 && retryCsrf && !SAFE_METHODS.has(String(options.method || 'GET').toUpperCase())) {
      const body = await response.clone().json().catch(() => null);
      if (body?.codigo === 'CSRF_INVALID') {
        csrfToken = null;
        return csrfFetch(apiBaseUrl, path, options, loginPath, false);
      }
    }
    return response;
  };

  const authenticatedFetch = async (apiBaseUrl, path, options = {}, loginPath) =>
    csrfFetch(apiBaseUrl, path, options, loginPath);

  const recoverSession = async (apiBaseUrl, profiles, loginPath) => {
    const current = read();
    if (isValid(current)) return requireSession(profiles, loginPath);

    const response = await fetch(`${apiBaseUrl}/session`, { credentials: 'include' });
    if (response.status === 401) {
      await handleUnauthorized(response, loginPath);
      return null;
    }
    if (!response.ok) throw new Error('Nao foi possivel recuperar a sessao.');
    const recovered = await response.json();
    sessionStorage.setItem(SESSION_KEY, JSON.stringify(recovered));
    return requireSession(profiles, loginPath);
  };

  const logout = async (apiBaseUrl, loginPath) => {
    let response;
    try {
      response = await authenticatedFetch(apiBaseUrl, '/logout', { method: 'POST' }, loginPath);
    } catch {
      const authenticationRedirect = /[?&]auth=(expired|invalid)(?:&|$)/.test(window.location.href);
      sessionStorage.removeItem(SESSION_KEY);
      sessionStorage.removeItem(LEGACY_USER_KEY);
      if (!authenticationRedirect) {
        window.location.href = `${loginPath}?auth=logout_failed`;
      }
      return false;
    }

    const authenticationRedirect = /[?&]auth=(expired|invalid)(?:&|$)/.test(window.location.href);
    if (authenticationRedirect) return false;

    sessionStorage.removeItem(SESSION_KEY);
    sessionStorage.removeItem(LEGACY_USER_KEY);
    if (response.status !== 204) {
      window.location.href = `${loginPath}?auth=logout_failed`;
      return false;
    }
    window.location.href = `${loginPath}?auth=logout`;
    return true;
  };

  const requireSession = (profiles, loginPath) => {
    const session = read();
    const profile = String(session?.perfil || '').trim().toLowerCase();
    if (!isValid(session) || !profiles.includes(profile)) {
      clearAndRedirect(loginPath, isValid(session) ? 'forbidden' : 'expired');
      return null;
    }
    return { ...session, perfil: profile };
  };

  const handleUnauthorized = async (response, loginPath) => {
    if (response.status !== 401) return false;
    const body = await response.json().catch(() => null);
    const reason = body?.codigo === 'TOKEN_EXPIRED' ? 'expired' : 'invalid';
    clearAndRedirect(loginPath, reason);
    return true;
  };

  global.jwtSession = Object.freeze({
    read,
    isValid,
    requireSession,
    clearAndRedirect,
    handleUnauthorized,
    csrfFetch,
    authenticatedFetch,
    recoverSession,
    logout
  });
})(window);
