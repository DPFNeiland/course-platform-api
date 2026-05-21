document.addEventListener('DOMContentLoaded', function() {
  // 1. Proteção de rota: bloquear se não tiver sessionStorage válido para professor
  const userStr = sessionStorage.getItem('user');
  if (!userStr) {
    window.location.href = '/index.html';
    return;
  }
  let user;
  try {
    user = JSON.parse(userStr);
  } catch (e) {
    sessionStorage.removeItem('user');
    window.location.href = '/index.html';
    return;
  }
  if (!user.id || !user.nome || user.perfil !== 'professor') {
    sessionStorage.removeItem('user');
    window.location.href = '/index.html';
    return;
  }

  // 2. Função logout
  window.logout = function() {
    sessionStorage.clear();
    window.location.href = '/index.html';
  };

  // 6. Funcionalidade de envio de mensagem no chat (adiciona localmente)
  window.sendMessage = function() {
    const input = document.querySelector('#chat-input');
    if (!input) return;
    const value = input.value.trim();
    if (!value) return;
    const messages = document.querySelector('#chat-messages');
    if (!messages) return;
    const msg = document.createElement('div');
    msg.className = 'message sent';
    msg.innerHTML = `
      <strong>${user.nome}:</strong> ${value}
      <small>${new Date().toLocaleTimeString('pt-BR')}</small>
    `;
    messages.appendChild(msg);
    messages.scrollTop = messages.scrollHeight;
    input.value = '';
    input.focus();
  };

  // 7. Placeholders de funções
  window.exportRelatorio = function() {
    console.log('Exportar relatório - Implementar');
    alert('Funcionalidade de exportar relatório em desenvolvimento.');
  };
  window.uploadMaterial = function() {
    console.log('Upload de materiais - Implementar');
    alert('Funcionalidade de upload de materiais em desenvolvimento.');
  };
  window.filtrarAgenda = function() {
    console.log('Filtros de agenda - Implementar');
    alert('Filtros de agenda em desenvolvimento.');
  };
  window.filtrarAvaliacoes = function() {
    console.log('Filtros de avaliações - Implementar');
    alert('Filtros de avaliações em desenvolvimento.');
  };

  // Event delegation para interações
  document.addEventListener('click', function(e) {
    // 3. Manipulação de abas (Chat / Participantes)
    const tabBtn = e.target.closest('.tab-btn');
    if (tabBtn && tabBtn.dataset.tab) {
      const panel = tabBtn.dataset.tab; // e.g., 'chat', 'participantes'
      document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
      document.querySelectorAll('.tab-panel').forEach(p => p.classList.remove('active'));
      tabBtn.classList.add('active');
      const activePanel = document.querySelector(`.tab-panel[data-panel="${panel}"]`);
      if (activePanel) activePanel.classList.add('active');
    }

    // 4. Toggle de microfone e câmera
    if (e.target.matches('#mic-toggle')) {
      e.target.classList.toggle('active');
      console.log('Microfone toggled');
    }
    if (e.target.matches('#cam-toggle')) {
      e.target.classList.toggle('active');
      console.log('Câmera toggled');
    }

    // Logout
    if (e.target.matches('#logout-btn, .logout-btn')) {
      logout();
    }

    // 5. Abertura de modais (data-modal)
    if (e.target.matches('[data-modal]')) {
      const modalId = e.target.dataset.modal;
      const modal = document.getElementById(modalId);
      if (modal) modal.classList.add('open');
    }

    // Fechamento de modais
    if (e.target.matches('.modal-close') || e.target.closest('.modal-close')) {
      const modal = e.target.closest('.modal');
      if (modal) modal.classList.remove('open');
    }
    // Fechar modal clicando no overlay
    if (e.target.classList.contains('modal') && e.target.classList.contains('open')) {
      e.target.classList.remove('open');
    }

    // Chamada de ações placeholder (data-action="exportRelatorio")
    if (e.target.matches('[data-action]')) {
      const action = e.target.dataset.action;
      if (typeof window[action] === 'function') {
        window[action]();
      }
    }
  });

  // Enter no chat input
  const chatInput = document.querySelector('#chat-input');
  if (chatInput) {
    chatInput.addEventListener('keypress', function(e) {
      if (e.key === 'Enter') {
        sendMessage();
      }
    });
  }
});