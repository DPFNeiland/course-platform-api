let appState = {
  currentFilter: '',
  searchQuery: '',
  session: null
};

const updateProgressBars = () => {
  document.querySelectorAll('.progress').forEach(bar => {
    const progress = bar.getAttribute('data-progress');
    if (progress !== null) {
      bar.style.width = `${progress}%`;
    }
  });
};

const openPlanModal = () => {
  const modalOverlay = document.querySelector('.modal-overlay');
  if (modalOverlay) {
    modalOverlay.classList.add('active');
  }
};

const closePlanModal = () => {
  const modalOverlay = document.querySelector('.modal-overlay');
  if (modalOverlay) {
    modalOverlay.classList.remove('active');
  }
};

const logout = () => {
  sessionStorage.clear();
  window.location.href = '/index.html';
};

const renderCatalog = () => {
  console.log('Rendering catalog with filter:', appState.currentFilter, 'search:', appState.searchQuery);
  // Placeholder for actual catalog rendering
};

const postDoubt = () => {
  const textarea = document.querySelector('.doubt-textarea');
  if (!textarea) return;

  const text = textarea.value.trim();
  if (!text) return;

  const discussionsContainer = document.querySelector('.discussions');
  if (!discussionsContainer) return;

  const doubtEl = document.createElement('div');
  doubtEl.className = 'discussion-item';
  doubtEl.innerHTML = `
    <p>${text}</p>
    <button class="useful-btn">Útil</button>
    <span class="useful-count">(0)</span>
  `;

  discussionsContainer.appendChild(doubtEl);
  textarea.value = '';
};

const handleClick = (e) => {
  if (e.target.matches('.filter-btn')) {
    document.querySelectorAll('.filter-btn').forEach(btn => btn.classList.remove('active'));
    e.target.classList.add('active');
    appState.currentFilter = e.target.dataset.filter || e.target.textContent.trim();
    renderCatalog();
  } else if (e.target.matches('.open-plan-modal')) {
    openPlanModal();
  } else if (e.target.matches('.close-plan-modal') || e.target.matches('.modal-overlay')) {
    closePlanModal();
  } else if (e.target.matches('.logout-btn')) {
    logout();
  } else if (e.target.matches('.post-doubt-btn')) {
    postDoubt();
  } else if (e.target.matches('.useful-btn')) {
    const item = e.target.closest('.discussion-item');
    if (item) {
      const countSpan = item.querySelector('.useful-count');
      if (countSpan) {
        const match = countSpan.textContent.match(/\d+/);
        let count = match ? parseInt(match[0]) : 0;
        countSpan.textContent = `(${count + 1})`;
      }
    }
  }
};

const handleInput = (e) => {
  if (e.target.matches('.search-bar')) {
    appState.searchQuery = e.target.value;
    renderCatalog();
  }
};

document.addEventListener('DOMContentLoaded', () => {
  // Route protection
  const sessionStr = sessionStorage.getItem('session');
  if (!sessionStr) {
    window.location.href = '/index.html';
    return;
  }

  try {
    appState.session = JSON.parse(sessionStr);
    if (appState.session.perfil !== 'aluno') {
      window.location.href = '/index.html';
      return;
    }
  } catch (err) {
    window.location.href = '/index.html';
    return;
  }

  // Fill dynamic name
  const nomeAluno = appState.session.nome || 'Aluno';
  document.querySelectorAll('[data-field="nomeAluno"]').forEach(el => {
    el.textContent = nomeAluno;
  });

  // Update progress bars
  updateProgressBars();

  // Event delegation
  document.addEventListener('click', handleClick);
  document.addEventListener('input', handleInput);
});