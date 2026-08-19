(function(global) {
  const LOADING_CLASSES = new Set([
    'loading-skeleton',
    'loading-skeleton-text',
    'loading-skeleton-avatar',
    'loading-skeleton-card',
    'loading-skeleton-chip'
  ]);

  const finishLoading = element => {
    if (!element) return;
    element.removeAttribute('aria-busy');
    element.removeAttribute('aria-label');
    element.removeAttribute('aria-hidden');
    if (typeof element.className === 'string') {
      element.className = element.className
        .split(/\s+/)
        .filter(className => className && !LOADING_CLASSES.has(className))
        .join(' ');
    }
  };

  global.dashboardUI = Object.freeze({ finishLoading });
})(window);
