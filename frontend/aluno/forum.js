(function() {
  'use strict';

  let nomeAluno;

  function protectRoute() {
    const sessionStr = sessionStorage.getItem('session');
    if (!sessionStr) {
      window.location.href = '/index.html';
      return;
    }
    const session = JSON.parse(sessionStr);
    if (session.perfil !== 'aluno') {
      window.location.href = '/index.html';
      return;
    }
    nomeAluno = session.nome || 'Aluno';
  }

  function loadAlunoName() {
    document.querySelectorAll('[data-field="nomeAluno"]').forEach(el => {
      el.textContent = nomeAluno;
    });
  }

  function logout() {
    sessionStorage.clear();
    window.location.href = '/index.html';
  }

  function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }

  function formatTimeAgo(date) {
    const now = new Date();
    const diff = now - date;
    const minutes = Math.floor(diff / 60000);
    if (minutes < 1) return 'agora';
    if (minutes < 60) return `${minutes} min atrás`;
    const hours = Math.floor(minutes / 60);
    return `${hours}h atrás`;
  }

  function createDoubtElement(name, text) {
    const now = new Date();
    const div = document.createElement('div');
    div.className = 'doubt';
    div.innerHTML = `
      <div class="avatar generic"></div>
      <div class="header">
        <span class="name">${escapeHtml(name)}</span>
        <span class="time">${formatTimeAgo(now)}</span>
      </div>
      <h3>Dúvida enviada</h3>
      <p class="text">${escapeHtml(text)}</p>
      <div class="actions">
        <button class="reply-btn" data-action="reply">Responder</button>
        <button class="useful-btn" data-action="useful">Útil <span class="count">0</span></button>
      </div>
    `;
    return div;
  }

  function createReplyForm() {
    const div = document.createElement('div');
    div.className = 'reply-form';
    div.innerHTML = `
      <textarea class="reply-text" placeholder="Digite sua resposta..."></textarea>
      <button class="send-reply-btn" data-action="send-reply">Enviar Resposta</button>
    `;
    return div;
  }

  function createResponseElement(text) {
    const div = document.createElement('div');
    div.className = 'response';
    div.innerHTML = `
      <span class="badge">VOCÊ</span>
      <p class="response-text">${escapeHtml(text)}</p>
      <button class="response-useful" data-action="useful">Útil <span class="count">0</span></button>
    `;
    return div;
  }

  function postDoubt() {
    const textarea = document.querySelector('.new-doubt-text');
    if (!textarea) return;
    const text = textarea.value.trim();
    if (!text) {
      textarea.focus();
      return;
    }
    const container = document.querySelector('.forum-discussions');
    if (!container) return;
    const doubt = createDoubtElement(nomeAluno, text);
    container.appendChild(doubt);
    textarea.value = '';
  }

  function showReplyForm(doubt) {
    if (doubt.querySelector('.reply-form')) return;
    const form = createReplyForm();
    const actions = doubt.querySelector('.actions');
    actions.insertAdjacentElement('afterend', form);
    form.querySelector('.reply-text').focus();
  }

  function sendReply(btn) {
    const form = btn.closest('.reply-form');
    const textarea = form.querySelector('.reply-text');
    const text = textarea.value.trim();
    if (!text) {
      textarea.focus();
      return;
    }
    const response = createResponseElement(text);
    form.insertAdjacentElement('beforebegin', response);
    form.remove();
  }

  function incrementUseful(btn) {
    const countEl = btn.querySelector('.count');
    if (countEl) {
      const count = parseInt(countEl.textContent) || 0;
      countEl.textContent = count + 1;
    }
  }

  function handleClick(e) {
    const target = e.target.closest('[data-action]');
    if (!target) return;

    const action = target.dataset.action;

    if (action === 'post-doubt') {
      e.preventDefault();
      postDoubt();
    } else if (action === 'logout') {
      logout();
    } else if (action === 'reply') {
      e.preventDefault();
      const doubt = target.closest('.doubt');
      showReplyForm(doubt);
    } else if (action === 'send-reply') {
      e.preventDefault();
      sendReply(target);
    } else if (action === 'useful') {
      incrementUseful(target);
    }
  }

  function init() {
    protectRoute();
    loadAlunoName();
    document.addEventListener('click', handleClick);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }

})();