(function(global) {
  'use strict';

  const SESSION_KEY = 'session';
  const LEGACY_USER_KEY = 'user';
  const ISO_INSTANT = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d{1,9})?Z$/;

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

  const logout = async (apiBaseUrl, loginPath) => {
    try {
      await fetch(`${apiBaseUrl}/logout`, { method: 'POST', credentials: 'include' });
    } finally {
      clearAndRedirect(loginPath, 'logout');
    }
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

  global.jwtSession = Object.freeze({ read, isValid, requireSession, clearAndRedirect, handleUnauthorized, logout });
})(window);
