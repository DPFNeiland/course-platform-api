document.addEventListener('DOMContentLoaded', async function() {
  const API_BASE_URL = window.APP_CONFIG.API_BASE_URL;
  const state = { user: null, cursos: [], matriculas: [], alunos: [] };
  let agendaMonth = new Date();
  agendaMonth = new Date(agendaMonth.getFullYear(), agendaMonth.getMonth(), 1);

  const api = async (path, options = {}) => {
    const session = window.jwtSession.requireSession(['professor'], '../index.html');
    if (!session) throw new Error('Sessao expirada. Faca login novamente.');
    const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
    const response = await window.jwtSession.authenticatedFetch(
      API_BASE_URL, path, { ...options, headers }, '../index.html'
    );
    if (response.status === 401) {
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

  state.user = await window.jwtSession.recoverSession(API_BASE_URL, ['professor'], '../index.html');

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
  const isAgendaPage = () => Boolean(document.querySelector('.calendar-grid'));

  const finishLoading = element => {
    if (!element) return;
    element.removeAttribute('aria-busy');
    element.removeAttribute('aria-label');
    if (typeof element.className === 'string') {
      const loadingClasses = new Set([
        'loading-skeleton', 'loading-skeleton-text', 'loading-skeleton-card', 'loading-skeleton-chip'
      ]);
      element.className = element.className
        .split(/\s+/)
        .filter(className => className && !loadingClasses.has(className))
        .join(' ');
    }
  };

  const createElement = (tagName, className, text) => {
    const element = document.createElement(tagName);
    if (className) element.className = className;
    if (text !== undefined) element.textContent = text;
    return element;
  };

  const setAvatarAndName = () => {
    document.querySelectorAll('[data-field="nomeProfessor"]').forEach(el => {
      el.textContent = state.user.nome || 'Professor';
      finishLoading(el);
    });
    document.querySelectorAll('.avatar').forEach(el => {
      el.textContent = (state.user.nome || 'PR').slice(0, 2).toUpperCase();
      finishLoading(el);
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
      values.forEach(finishLoading);
      finishLoading(document.querySelector('.kpi-grid'));
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
      const cards = cursos.map(curso => {
        const card = createElement('article', 'card activity-card');
        card.appendChild(createElement('h4', '', curso.nome));
        card.appendChild(createElement(
          'p',
          '',
          `${formatEnum(curso.tipoCurso)} - ${formatEnum(curso.tipoAssinatura)}`
        ));
        const meta = createElement('div', 'meta');
        meta.appendChild(createElement('span', 'pill bg-blue', formatEnum(curso.status)));
        card.appendChild(meta);
        return card;
      });
      activities.replaceChildren(...(cards.length
        ? cards
        : [createElement('p', 'empty-state', 'Nenhum curso cadastrado ainda.')]
      ));
      finishLoading(activities);
    }

    const charts = document.querySelector('.charts-grid');
    if (charts && document.querySelector('#courseForm')) {
      const statusCard = createElement('div', 'card chart-card');
      statusCard.append(
        createElement('h3', '', 'Cursos por status'),
        createElement('p', '', `Aprovados: ${cursos.filter(c => c.status === 'APROVADO').length}`),
        createElement('p', '', `Em avaliação: ${cursos.filter(c => c.status === 'EM_AVALIACAO').length}`),
        createElement('p', '', `Reavaliar: ${cursos.filter(c => c.status === 'REAVALIAR').length}`)
      );
      const enrollmentsCard = createElement('div', 'card chart-card');
      enrollmentsCard.append(
        createElement('h3', '', 'Matrículas reais'),
        createElement('p', '', `${matriculas.length} matrícula(s) vinculada(s) aos seus cursos.`)
      );
      charts.replaceChildren(statusCard, enrollmentsCard);
      finishLoading(charts);
    }
  };

  const renderDashboardError = error => {
    const values = document.querySelectorAll('.kpi-card .value');
    if (!values.length) return false;
    values.forEach(value => {
      value.textContent = '--';
      finishLoading(value);
    });
    finishLoading(document.querySelector('.kpi-grid'));

    const message = error?.message || 'Dados do dashboard indisponíveis.';
    const activities = document.querySelector('.activities-grid');
    if (activities) {
      activities.replaceChildren(createElement('p', 'dashboard-unavailable', message));
      finishLoading(activities);
    }
    const charts = document.querySelector('.charts-grid');
    if (charts) {
      charts.replaceChildren(createElement('p', 'dashboard-unavailable', message));
      finishLoading(charts);
    }
    return true;
  };

  const dateKey = date => [
    date.getFullYear(),
    String(date.getMonth() + 1).padStart(2, '0'),
    String(date.getDate()).padStart(2, '0')
  ].join('-');

  const renderCalendar = () => {
    const grid = document.querySelector('.calendar-grid');
    const title = document.querySelector('.month-title');
    if (!grid || !title) return;

    const year = agendaMonth.getFullYear();
    const month = agendaMonth.getMonth();
    const firstWeekday = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const cellCount = Math.ceil((firstWeekday + daysInMonth) / 7) * 7;
    const today = new Date();
    const days = [];

    for (let index = 0; index < cellCount; index += 1) {
      const date = new Date(year, month, index - firstWeekday + 1);
      const day = document.createElement('div');
      const isCurrentMonth = date.getMonth() === month;
      const isToday = dateKey(date) === dateKey(today);
      day.className = ['day', !isCurrentMonth && 'other-month', isToday && 'today']
        .filter(Boolean)
        .join(' ');
      day.dataset.date = dateKey(date);
      day.setAttribute('aria-label', date.toLocaleDateString('pt-BR', {
        day: 'numeric', month: 'long', year: 'numeric'
      }));

      const number = document.createElement('span');
      number.className = 'date';
      number.textContent = date.getDate();
      day.appendChild(number);
      days.push(day);
    }

    const monthLabel = agendaMonth.toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' });
    title.textContent = monthLabel.charAt(0).toUpperCase() + monthLabel.slice(1);
    grid.replaceChildren(...days);
    document.querySelectorAll('[data-month-direction]').forEach(button => {
      button.disabled = false;
    });
    finishLoading(document.querySelector('.calendar-section'));
  };

  const renderAgendaCourses = () => {
    const events = document.querySelector('.events-list');
    if (!events) return;
    const cards = ownCourses().map(curso => {
      const card = document.createElement('article');
      card.className = 'event-card';

      const type = document.createElement('div');
      type.className = 'event-time';
      type.textContent = 'Curso vinculado';

      const title = document.createElement('div');
      title.className = 'event-title';
      title.textContent = curso.nome || `Curso #${curso.id}`;

      const status = document.createElement('span');
      status.className = 'badge aula event-badge';
      status.textContent = formatEnum(curso.status) || 'Status indisponível';

      card.append(type, title, status);
      return card;
    });

    if (!cards.length) {
      const empty = document.createElement('p');
      empty.className = 'empty-state';
      empty.textContent = 'Nenhum curso vinculado ao seu perfil.';
      cards.push(empty);
    }
    events.replaceChildren(...cards);
    finishLoading(document.querySelector('.events-section'));
  };

  const renderAgendaError = error => {
    if (!isAgendaPage()) return false;
    renderCalendar();
    const events = document.querySelector('.events-list');
    if (events) {
      const message = document.createElement('p');
      message.className = 'empty-state';
      message.textContent = error?.message || 'Não foi possível carregar os cursos.';
      events.replaceChildren(message);
    }
    finishLoading(document.querySelector('.events-section'));
    return true;
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
    if (isAgendaPage()) {
      renderCalendar();
      const cursos = await api(`/curso/professor/${state.user.id}`);
      if (!Array.isArray(cursos)) throw new Error('Resposta de cursos inválida.');
      state.cursos = cursos;
      renderAgendaCourses();
      return;
    }

    const [cursos, matriculas, alunos] = await Promise.all([
      api('/curso'),
      api('/matricula'),
      api('/aluno').catch(() => [])
    ]);
    if (!Array.isArray(cursos)) throw new Error('Resposta de cursos inválida.');
    if (!Array.isArray(matriculas)) throw new Error('Resposta de matrículas inválida.');
    if (!Array.isArray(alunos)) throw new Error('Resposta de alunos inválida.');
    state.cursos = cursos;
    state.matriculas = matriculas;
    state.alunos = alunos;
    renderDashboard();
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
    const monthButton = e.target.closest('[data-month-direction]');
    if (monthButton && !monthButton.disabled) {
      const direction = Number(monthButton.dataset.monthDirection);
      agendaMonth = new Date(agendaMonth.getFullYear(), agendaMonth.getMonth() + direction, 1);
      renderCalendar();
    }
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
    if (renderAgendaError(err)) return;
    if (renderDashboardError(err)) return;
    document.querySelector('main, .section, .container')?.insertAdjacentHTML('afterbegin', `<p class="empty-state">${err.message}</p>`);
  });
});
