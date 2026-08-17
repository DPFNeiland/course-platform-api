document.addEventListener('DOMContentLoaded', async function() {
  const API_BASE_URL = 'http://localhost:8080';
  const state = { user: null, cursos: [], matriculas: [], alunos: [] };

  const api = async (path, options = {}) => {
    const session = window.jwtSession.requireSession(['professor'], '../index.html');
    if (!session) throw new Error('Sessao expirada. Faca login novamente.');
    const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
    const response = await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers,
      credentials: 'include'
    });
    if (await window.jwtSession.handleUnauthorized(response, '../index.html')) {
      throw new Error('Sessao expirada. Faca login novamente.');
    }
    if (!response.ok) {
      const errorData = await response.json().catch(() => null);
      throw new Error(errorData?.message || errorData?.error || 'Nao foi possivel concluir a operacao.');
    }
    return response.status === 204 ? null : response.json();
  };

  const formatEnum = value => String(value || '')
    .toLowerCase()
    .replaceAll('_', ' ')
    .replace(/\b\w/g, letter => letter.toUpperCase());

  state.user = window.jwtSession.requireSession(['professor'], '../index.html');

  const perfil = String(state.user?.perfil || state.user?.cargo || '').trim().toLowerCase();
  if (!state.user?.id || perfil !== 'professor') {
    window.location.href = '../index.html';
    return;
  }
  state.user.perfil = perfil;
  sessionStorage.setItem('session', JSON.stringify(state.user));

  const ownCourses = () => state.cursos.filter(curso => Number(curso.professorId) === Number(state.user.id));
  const matriculasDoProfessor = () => state.matriculas.filter(matricula => ownCourses().some(curso => Number(curso.id) === Number(matricula.cursoId)));
  const alunoPorId = id => state.alunos.find(aluno => Number(aluno.id) === Number(id));
  const cursoPorId = id => state.cursos.find(curso => Number(curso.id) === Number(id));

  const setAvatarAndName = () => {
    document.querySelectorAll('[data-field="nomeProfessor"]').forEach(el => {
      el.textContent = state.user.nome || 'Professor';
    });
    document.querySelectorAll('.avatar').forEach(el => {
      el.textContent = (state.user.nome || 'PR').slice(0, 2).toUpperCase();
    });
  };

  const renderDashboard = () => {
    const cursos = ownCourses();
    const matriculas = matriculasDoProfessor();
    const values = document.querySelectorAll('.kpi-card .value');
    if (values.length) {
      values[0].textContent = cursos.filter(c => c.status === 'APROVADO').length;
      values[1].textContent = cursos.filter(c => c.status === 'EM_AVALIACAO').length;
      values[2].textContent = matriculas.length;
      values[3].textContent = cursos.length;
    }

    const labels = document.querySelectorAll('.kpi-card .label');
    if (labels.length) {
      labels[0].textContent = 'Cursos aprovados';
      labels[1].textContent = 'Cursos em avaliacao';
      labels[2].textContent = 'Matriculas recebidas';
      labels[3].textContent = 'Cursos cadastrados';
    }

    const activities = document.querySelector('.activities-grid');
    if (activities) {
      activities.innerHTML = cursos.length
        ? cursos.map(curso => `
          <div class="card activity-card">
            <h4>${curso.nome}</h4>
            <p>${formatEnum(curso.tipoCurso)} - ${formatEnum(curso.tipoAssinatura)}</p>
            <div class="meta"><span class="pill bg-blue">${formatEnum(curso.status)}</span></div>
          </div>
        `).join('')
        : '<p class="empty-state">Nenhum curso cadastrado ainda.</p>';
    }

    const charts = document.querySelector('.charts-grid');
    if (charts && document.querySelector('#courseForm')) {
      charts.innerHTML = `
        <div class="card chart-card"><h3>Cursos por status</h3><p>Aprovados: ${cursos.filter(c => c.status === 'APROVADO').length}</p><p>Em avaliacao: ${cursos.filter(c => c.status === 'EM_AVALIACAO').length}</p><p>Reavaliar: ${cursos.filter(c => c.status === 'REAVALIAR').length}</p></div>
        <div class="card chart-card"><h3>Matriculas reais</h3><p>${matriculas.length} matricula(s) vinculadas aos seus cursos.</p></div>
      `;
    }
  };

  const renderAgenda = () => {
    const events = document.querySelector('.events-list');
    if (!events) return;
    const cursos = ownCourses();
    events.innerHTML = cursos.length
      ? cursos.map(curso => `
        <div class="event-card">
          <div class="event-time">Curso cadastrado</div>
          <div class="event-title">${curso.nome}</div>
          <span class="badge aula event-badge">${formatEnum(curso.status)}</span>
        </div>
      `).join('')
      : '<p class="empty-state">Nao ha eventos reais no backend. Cadastre cursos para acompanha-los aqui.</p>';
    const month = document.querySelector('.month-title');
    if (month) month.textContent = new Date().toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' });
  };

  const renderAvaliacoes = () => {
    document.querySelectorAll('.cards-grid, .horizontal-grid').forEach((container, index) => {
      if (!container.closest('.ongoing') && index > 0) return;
      const cursos = ownCourses();
      container.innerHTML = cursos.length
        ? cursos.map(curso => {
          const total = state.matriculas.filter(m => Number(m.cursoId) === Number(curso.id)).length;
          return `
            <article class="${container.classList.contains('horizontal-grid') ? 'horizontal-card' : 'card'}">
              <h3>${curso.nome}</h3>
              <p class="meta">${formatEnum(curso.tipoCurso)} / ${formatEnum(curso.tipoAssinatura)}</p>
              <p class="date">Status: ${formatEnum(curso.status)}</p>
              <div class="progress"><span>${total} matricula(s)</span><div class="bar"><div class="fill" style="width: ${Math.min(total * 10, 100)}%;"></div></div></div>
              <span class="status corrigida">Dados do curso</span>
            </article>
          `;
        }).join('')
        : '<p class="empty-state">Avaliacoes ainda nao possuem endpoint no backend.</p>';
    });
  };

  const renderMateriais = () => {
    const materialGrid = document.querySelector('.materials-grid .grid, .cards-grid');
    if (!materialGrid || document.querySelector('#courseForm')) return;
    const cursos = ownCourses();
    materialGrid.innerHTML = cursos.length
      ? cursos.map(curso => `
        <article class="card material-card">
          <h3>${curso.nome}</h3>
          <p>${formatEnum(curso.status)}</p>
          <span>${formatEnum(curso.tipoCurso)} - ${formatEnum(curso.tipoAssinatura)}</span>
        </article>
      `).join('')
      : '<p class="empty-state">Materiais e upload ainda nao possuem endpoint no backend.</p>';
  };

  const renderDesempenho = () => {
    const kpis = document.querySelectorAll('.kpi-number');
    const cursos = ownCourses();
    const matriculas = matriculasDoProfessor();
    if (kpis.length) {
      kpis[0].textContent = cursos.length;
      kpis[1].textContent = cursos.filter(c => c.status === 'APROVADO').length;
      kpis[2].textContent = matriculas.length;
      kpis[3].textContent = matriculas.filter(m => m.status === 'ENCERRADA').length;
    }

    const tbody = document.querySelector('.table-container tbody');
    if (tbody) {
      tbody.innerHTML = matriculas.length
        ? matriculas.map(matricula => {
          const aluno = alunoPorId(matricula.alunoId);
          const curso = cursoPorId(matricula.cursoId);
          return `
            <tr>
              <td>${aluno?.nome || `Aluno #${matricula.alunoId}`}</td>
              <td>${curso?.nome || `Curso #${matricula.cursoId}`}</td>
              <td>${formatEnum(matricula.status)}</td>
              <td>${matricula.dataMatricula || '-'}</td>
              <td><div class="progress-bar"><div class="progress-fill" style="width: ${matricula.status === 'ENCERRADA' ? 100 : 25}%"></div></div></td>
            </tr>
          `;
        }).join('')
        : '<tr><td colspan="5">Nenhuma matricula encontrada para seus cursos.</td></tr>';
    }

    const charts = document.querySelector('.charts-grid');
    if (charts && !document.querySelector('#courseForm')) {
      charts.innerHTML = `
        <div class="card"><h3 class="section-title">Resumo real</h3><p>${cursos.length} curso(s), ${matriculas.length} matricula(s).</p></div>
        <div class="card"><h3 class="section-title">Observacao</h3><p>Notas, presenca e engajamento ainda nao possuem endpoints no backend.</p></div>
      `;
    }
  };

  const renderAulaAoVivo = () => {
    const participantes = document.querySelector('.participants ul, .participants-list');
    if (participantes) {
      const matriculas = matriculasDoProfessor();
      participantes.innerHTML = matriculas.length
        ? matriculas.map(m => `<div class="participant"><div class="avatar">${(alunoPorId(m.alunoId)?.nome || 'AL').slice(0, 2).toUpperCase()}</div><span>${alunoPorId(m.alunoId)?.nome || `Aluno #${m.alunoId}`}</span></div>`).join('')
        : '<p class="empty-state">Nenhum aluno matriculado nos seus cursos.</p>';
    }
    const messages = document.querySelector('#chat-messages, .chat-messages');
    if (messages && !messages.dataset.ready) {
      messages.dataset.ready = 'true';
      messages.innerHTML = '<p class="empty-state">Chat ao vivo nao possui endpoint no backend. Mensagens enviadas ficam apenas nesta tela.</p>';
    }
  };

  const renderForum = () => {
    const discussions = document.querySelector('.forum-grid, .topics-grid, .topics-list, .discussions-list');
    if (discussions) {
      discussions.innerHTML = '<p class="empty-state">Forum do professor ainda nao possui endpoint no backend.</p>';
    }
  };

  const refreshData = async () => {
    const [cursos, matriculas, alunos] = await Promise.all([
      api('/curso'),
      api('/matricula'),
      api('/aluno').catch(() => [])
    ]);
    state.cursos = cursos;
    state.matriculas = matriculas;
    state.alunos = alunos;
    renderDashboard();
    renderAgenda();
    renderAvaliacoes();
    renderMateriais();
    renderDesempenho();
    renderAulaAoVivo();
    renderForum();
  };

  const courseForm = document.getElementById('courseForm');
  if (courseForm) {
    courseForm.addEventListener('submit', async function(e) {
      e.preventDefault();
      const feedback = document.getElementById('course-feedback');
      const payload = {
        nome: document.getElementById('course-nome').value,
        tipoAssinatura: document.getElementById('course-tipoAssinatura').value,
        tipoCurso: document.getElementById('course-tipoCurso').value,
        professorId: state.user.id
      };

      try {
        await api('/curso', { method: 'POST', body: JSON.stringify(payload) });
        courseForm.reset();
        if (feedback) feedback.textContent = 'Curso enviado para avaliacao do curador.';
        await refreshData();
      } catch (err) {
        if (feedback) feedback.textContent = err.message;
      }
    });
  }

  window.logout = logout;
  window.sendMessage = function() {
    const input = document.querySelector('#chat-input, .chat-input input');
    const messages = document.querySelector('#chat-messages, .chat-messages');
    const value = input?.value.trim();
    if (!value || !messages) return;
    const msg = document.createElement('div');
    msg.className = 'message sent';
    msg.innerHTML = `<strong>${state.user.nome}:</strong> ${value}<small>${new Date().toLocaleTimeString('pt-BR')}</small>`;
    messages.appendChild(msg);
    input.value = '';
  };

  function logout() {
    window.jwtSession.logout(API_BASE_URL, '../index.html');
  }

  document.addEventListener('click', function(e) {
    const tabBtn = e.target.closest('.tab-btn');
    if (tabBtn && tabBtn.dataset.tab) {
      document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
      document.querySelectorAll('.tab-panel').forEach(panel => panel.classList.remove('active'));
      tabBtn.classList.add('active');
      document.querySelector(`.tab-panel[data-panel="${tabBtn.dataset.tab}"]`)?.classList.add('active');
    }
    if (e.target.matches('#logout-btn, .logout-btn')) logout();
    if (e.target.matches('.chat-input button')) sendMessage();
    if (e.target.matches('[data-action]')) {
      alert('Esta funcionalidade ainda nao possui endpoint no backend.');
    }
  });

  setAvatarAndName();
  await refreshData().catch(err => {
    document.querySelector('main, .section, .container')?.insertAdjacentHTML('afterbegin', `<p class="empty-state">${err.message}</p>`);
  });
});
