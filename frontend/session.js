(function(global) {
  'use strict';

  const SESSION_KEY = 'session';
  const ISO_INSTANT = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d{1,9})?Z$/;

  const read = () => {
    try {
      const value = sessionStorage.getItem(SESSION_KEY);
      return value ? JSON.parse(value) : null;
    } catch {
      return null;
    }
  };

  const isValid = session => {
    if (!session || typeof session.token !== 'string' || session.token.split('.').length !== 3) return false;
    if (typeof session.expiraEm !== 'string' || !ISO_INSTANT.test(session.expiraEm)) return false;
    const expiration = Date.parse(session.expiraEm);
    return Number.isFinite(expiration) && expiration > Date.now();
  };

  const clearAndRedirect = (loginPath, reason = 'invalid') => {
    sessionStorage.removeItem(SESSION_KEY);
    window.location.href = `${loginPath}?auth=${encodeURIComponent(reason)}`;
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

  global.jwtSession = Object.freeze({ read, isValid, requireSession, clearAndRedirect, handleUnauthorized });
})(window);
