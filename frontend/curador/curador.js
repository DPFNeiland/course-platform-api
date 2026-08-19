document.addEventListener('DOMContentLoaded', async function() {
  const API_BASE_URL = window.APP_CONFIG.API_BASE_URL;
  const state = {
    user: null,
    cursos: [],
    matriculas: [],
    alunos: [],
    professores: [],
    usuarios: [],
    monitoringAvailability: null
  };

  const api = async (path, options = {}) => {
    const session = window.jwtSession.requireSession(['curador', 'admin'], '../index.html');
    if (!session) throw new Error('Sessao expirada. Faca login novamente.');
    const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
    const response = await window.jwtSession.authenticatedFetch(
      API_BASE_URL, path, { ...options, headers }, '../index.html'
    );
    if (response.status === 401) {
      throw new Error('Sessao expirada. Faca login novamente.');
    }
    if (!response.ok) {
      const error = await response.json().catch(() => null);
      throw new Error(error?.message || error?.error || 'Nao foi possivel concluir a operacao.');
    }
    return response.status === 204 ? null : response.json();
  };

  const formatEnum = value => String(value || '')
    .toLowerCase()
    .replaceAll('_', ' ')
    .replace(/\b\w/g, letter => letter.toUpperCase());

  const cursoPorId = id => state.cursos.find(curso => Number(curso.id) === Number(id));
  const alunoPorId = id => state.alunos.find(aluno => Number(aluno.id) === Number(id));
  const professorPorId = id => state.professores.find(prof => Number(prof.id) === Number(id));
  const catalogContainer = () => {
    const container = document.querySelector('.courses-grid, [data-curator-courses]');
    return container && !document.querySelector('[data-pending-courses]') ? container : null;
  };
  const isMonitoringPage = () => Boolean(
    document.querySelector('.kpis') && document.querySelector('.table-section')
  );

  const finishCatalogLoading = container => {
    container.removeAttribute('aria-busy');
    container.removeAttribute('aria-label');
  };

  const createElement = (tagName, className, text) => {
    const element = document.createElement(tagName);
    if (className) element.className = className;
    if (text !== undefined) element.textContent = text;
    return element;
  };

  const finishLoading = element => {
    // As demais telas reutilizam este módulo sem carregar dashboard-ui.js.
    if (window.dashboardUI?.finishLoading) {
      window.dashboardUI.finishLoading(element);
      return;
    }
    if (!element) return;
    element.removeAttribute('aria-busy');
    element.removeAttribute('aria-label');
    element.removeAttribute('aria-hidden');
  };

  const renderIdentityError = () => {
    document.querySelectorAll('.avatar').forEach(el => {
      el.textContent = '--';
      finishLoading(el);
    });
    document.querySelectorAll('[data-field="nomeCurador"]').forEach(el => {
      el.textContent = 'Nome indisponível';
      finishLoading(el);
    });
  };

  const updateIdentity = () => {
    document.querySelectorAll('.avatar').forEach(el => {
      el.textContent = (state.user.nome || 'CU').slice(0, 2).toUpperCase();
      finishLoading(el);
    });
    document.querySelectorAll('[data-field="nomeCurador"]').forEach(el => {
      el.textContent = state.user.nome || 'Curador';
      finishLoading(el);
    });
  };

  const renderPendingCourses = () => {
    const container = document.querySelector('[data-pending-courses]');
    if (!container) return;
    const cursos = state.cursos.filter(curso => curso.status === 'EM_AVALIACAO' || curso.status === 'REAVALIAR');
    container.innerHTML = cursos.length
      ? cursos.map(curso => `
        <article class="course-card" data-course-id="${curso.id}">
          <details open>
            <summary>
              <div class="card-header">
                <div>
                  <h2>${curso.nome}</h2>
                  <div class="meta">
                    <span>${professorPorId(curso.professorId)?.nome || `Professor #${curso.professorId}`}</span>
                    <span>Tipo: ${formatEnum(curso.tipoCurso)}</span>
                    <span>Assinatura: ${formatEnum(curso.tipoAssinatura)}</span>
                  </div>
                </div>
                <span class="status-pill">${formatEnum(curso.status)}</span>
              </div>
            </summary>
            <div class="card-content">
              <p>Curso aguardando decisao do curador.</p>
              <div class="actions">
                <button class="btn-approve approve-course" data-id="${curso.id}">Aprovar</button>
                <button class="btn-reject reject-course" data-id="${curso.id}">Pedir reavaliacao</button>
              </div>
            </div>
          </details>
        </article>
      `).join('')
      : '<p class="empty-state">Nenhum curso aguardando avaliacao.</p>';
  };

  const renderCatalog = () => {
    const container = catalogContainer();
    if (!container) return;
    finishCatalogLoading(container);

    if (!state.cursos.length) {
      container.replaceChildren(createElement('p', 'empty-state', 'Nenhum curso cadastrado.'));
      return;
    }

    const cards = state.cursos.map(curso => {
      const card = createElement('article', 'course-card');
      card.appendChild(createElement('div', 'course-thumb'));
      card.appendChild(createElement('h3', 'course-title', curso.nome));
      card.appendChild(createElement(
        'p',
        'course-desc',
        `${formatEnum(curso.tipoCurso)} - ${formatEnum(curso.tipoAssinatura)}`
      ));

      const badges = createElement('div', 'course-badges');
      badges.appendChild(createElement('span', 'badge-pill', formatEnum(curso.status)));
      badges.appendChild(createElement(
        'span',
        'badge-pill',
        professorPorId(curso.professorId)?.nome || `Professor #${curso.professorId}`
      ));
      card.appendChild(badges);

      const actions = createElement('div', 'actions');
      const approveButton = createElement('button', 'btn approve-course', 'Aprovar');
      approveButton.dataset.id = curso.id;
      actions.appendChild(approveButton);
      const rejectButton = createElement('button', 'btn reject-course', 'Reavaliar');
      rejectButton.dataset.id = curso.id;
      actions.appendChild(rejectButton);
      card.appendChild(actions);

      return card;
    });

    container.replaceChildren(...cards);
  };

  const renderCatalogError = error => {
    const container = catalogContainer();
    if (!container) return false;

    const message = document.createElement('p');
    message.className = 'empty-state';
    message.textContent = error?.message || 'Nao foi possivel carregar os cursos.';
    finishCatalogLoading(container);
    container.replaceChildren(message);
    return true;
  };

  const renderDashboard = () => {
    const cards = document.querySelectorAll('.cards-section .card');
    if (cards.length) {
      const totals = {
        'Aprovar Cursos': state.cursos.filter(c => c.status === 'EM_AVALIACAO' || c.status === 'REAVALIAR').length,
        Monitorar: state.matriculas.length,
        Gamificacao: 0,
        Usuarios: state.alunos.length + state.professores.length + state.usuarios.length,
        Relatorios: state.cursos.length
      };
      cards.forEach(card => {
        const title = card.querySelector('h3')?.textContent.trim();
        const count = totals[title];
        if (count !== undefined) {
          const summary = card.querySelector('[data-curator-dashboard-summary]');
          if (summary) {
            summary.textContent = `${count} registro(s) reais no banco.`;
            finishLoading(summary);
          }
        }
      });
    }
  };

  const renderDashboardError = error => {
    const summaries = document.querySelectorAll('[data-curator-dashboard-summary]');
    if (!summaries.length) return false;
    summaries.forEach(summary => {
      summary.textContent = error?.message || 'Dados indisponíveis.';
      summary.className = 'dashboard-unavailable';
      finishLoading(summary);
    });
    return true;
  };

  const renderUsers = () => {
    const tbody = document.querySelector('.table tbody');
    if (!tbody) return;
    const subclassIds = new Set([...state.alunos, ...state.professores].map(user => Number(user.id)));
    const rows = [
      ...state.alunos.map(user => ({ ...user, perfil: 'Aluno', status: user.status || 'Ativo' })),
      ...state.professores.map(user => ({ ...user, perfil: 'Professor', status: 'Ativo' })),
      ...state.usuarios
        .filter(user => !subclassIds.has(Number(user.id)))
        .map(user => ({ ...user, perfil: formatEnum(user.perfil || 'Curador'), status: 'Ativo' }))
    ];
    const search = document.querySelector('.search-bar')?.value.trim().toLowerCase() || '';
    const filter = document.querySelector('.filter-select')?.value || '';
    const filtered = rows.filter(user => {
      const matchesSearch = !search || user.nome?.toLowerCase().includes(search) || user.email?.toLowerCase().includes(search);
      const matchesFilter = !filter || filter === 'todos' || user.perfil.toLowerCase().includes(filter.toLowerCase());
      return matchesSearch && matchesFilter;
    });
    tbody.innerHTML = filtered.length
      ? filtered.map(user => `
        <tr>
          <td>${user.nome}</td>
          <td>${user.email}</td>
          <td>${user.perfil}</td>
          <td class="status ativo">${formatEnum(user.status)}</td>
          <td><button class="btn btn-info view-user" data-id="${user.id}">Ver</button></td>
        </tr>
      `).join('')
      : '<tr><td colspan="5">Nenhum usuario encontrado.</td></tr>';
  };

  const renderMonitoring = () => {
    const availability = state.monitoringAvailability || {
      cursos: true,
      matriculas: true,
      alunos: true,
      professores: true
    };
    const kpis = document.querySelectorAll('.kpi-number');
    if (kpis.length) {
      kpis[0].textContent = availability.cursos
        ? state.cursos.filter(c => c.status === 'APROVADO').length
        : '--';
      kpis[1].textContent = availability.professores ? state.professores.length : '--';
      kpis[2].textContent = availability.alunos ? state.alunos.length : '--';
      kpis[3].textContent = availability.matriculas ? state.matriculas.length : '--';
      finishLoading(document.querySelector('.kpis'));
    }
    const tbody = document.querySelector('.table-section tbody');
    if (tbody) {
      const rows = availability.matriculas && state.matriculas.length
        ? state.matriculas.map(matricula => {
          const curso = cursoPorId(matricula.cursoId);
          const aluno = alunoPorId(matricula.alunoId);
          const row = createElement('tr');
          const courseName = availability.cursos
            ? curso?.nome || `Curso #${matricula.cursoId}`
            : 'Curso indisponível';
          const studentName = availability.alunos
            ? aluno?.nome || `Aluno #${matricula.alunoId}`
            : 'Aluno indisponível';
          row.appendChild(createElement('td', '', courseName));
          row.appendChild(createElement('td', '', studentName));
          row.appendChild(createElement('td', '', formatEnum(matricula.status)));
          row.appendChild(createElement('td', '', matricula.dataMatricula || '-'));
          return row;
        })
        : [createElement('tr')];

      if (!availability.matriculas || !state.matriculas.length) {
        const text = availability.matriculas
          ? 'Nenhuma matrícula registrada.'
          : 'Matrículas indisponíveis.';
        const empty = createElement('td', 'monitoring-placeholder', text);
        empty.colSpan = 4;
        rows[0].appendChild(empty);
      }
      tbody.replaceChildren(...rows);
      finishLoading(document.querySelector('.table-section'));
    }
    const graphs = document.querySelector('.graphs');
    if (graphs) {
      const coursesCard = createElement('div', 'graph-card');
      coursesCard.appendChild(createElement('h3', '', 'Cursos por status'));
      if (availability.cursos) {
        coursesCard.appendChild(createElement('p', '', `Aprovados: ${state.cursos.filter(c => c.status === 'APROVADO').length}`));
        coursesCard.appendChild(createElement('p', '', `Em avaliação: ${state.cursos.filter(c => c.status === 'EM_AVALIACAO').length}`));
        coursesCard.appendChild(createElement('p', '', `Reavaliar: ${state.cursos.filter(c => c.status === 'REAVALIAR').length}`));
      } else {
        coursesCard.appendChild(createElement('p', 'monitoring-placeholder', 'Dados de cursos indisponíveis.'));
      }

      const enrollmentsCard = createElement('div', 'graph-card');
      enrollmentsCard.appendChild(createElement('h3', '', 'Matrículas'));
      const enrollmentText = availability.matriculas
        ? `${state.matriculas.length} registro(s) reais no banco.`
        : 'Dados de matrículas indisponíveis.';
      enrollmentsCard.appendChild(createElement(
        'p',
        availability.matriculas ? '' : 'monitoring-placeholder',
        enrollmentText
      ));

      graphs.replaceChildren(coursesCard, enrollmentsCard);
      finishLoading(graphs);
    }
  };

  const renderMonitoringError = () => {
    const kpis = document.querySelectorAll('.kpi-number');
    if (!kpis.length) return false;
    state.monitoringAvailability = {
      cursos: false,
      matriculas: false,
      alunos: false,
      professores: false
    };
    renderMonitoring();
    return true;
  };

  const renderGamification = () => {
    const main = document.querySelector('main');
    if (!main || !location.pathname.includes('gamificacao')) return;
    main.innerHTML = `
      <section class="container" style="padding: 2rem;">
        <h1>Gamificacao</h1>
        <p class="empty-state">O backend ainda nao possui endpoints para regras, badges, pontos ou recompensas.</p>
        <p>Dados reais disponiveis agora: ${state.matriculas.length} matricula(s) e ${state.cursos.length} curso(s).</p>
      </section>
    `;
  };

  const updateCourseStatus = async (id, status) => {
    await api(`/curso/${id}/status?novoStatus=${status}`, { method: 'PUT' });
    await refreshData();
  };

  async function refreshCatalog() {
    const professoresPromise = api('/professor').catch(() => []);
    state.cursos = await api('/curso');
    renderCatalog();

    professoresPromise.then(professores => {
      state.professores = professores;
      renderCatalog();
    });
  }

  async function refreshMonitoring() {
    const [cursos, matriculas, alunos, professores] = await Promise.allSettled([
      api('/curso'),
      api('/matricula'),
      api('/aluno'),
      api('/professor')
    ]);
    const hasArrayValue = result => result.status === 'fulfilled' && Array.isArray(result.value);
    state.monitoringAvailability = {
      cursos: hasArrayValue(cursos),
      matriculas: hasArrayValue(matriculas),
      alunos: hasArrayValue(alunos),
      professores: hasArrayValue(professores)
    };
    state.cursos = state.monitoringAvailability.cursos ? cursos.value : [];
    state.matriculas = state.monitoringAvailability.matriculas ? matriculas.value : [];
    state.alunos = state.monitoringAvailability.alunos ? alunos.value : [];
    state.professores = state.monitoringAvailability.professores ? professores.value : [];
    renderMonitoring();
  }

  async function refreshData() {
    if (catalogContainer()) {
      await refreshCatalog();
      return;
    }
    if (isMonitoringPage()) {
      await refreshMonitoring();
      return;
    }

    const [cursos, matriculas, alunos, professores, usuarios] = await Promise.all([
      api('/curso'),
      api('/matricula'),
      api('/aluno').catch(() => []),
      api('/professor').catch(() => []),
      api('/usuario').catch(() => [])
    ]);
    state.cursos = cursos;
    state.matriculas = matriculas;
    state.alunos = alunos;
    state.professores = professores;
    state.usuarios = usuarios;
    renderDashboard();
    renderPendingCourses();
    renderCatalog();
    renderUsers();
    renderMonitoring();
    renderGamification();
  }

  function logout() {
    window.jwtSession.logout(API_BASE_URL, '../index.html');
  }

  document.addEventListener('click', function(e) {
    if (e.target.matches('#logout-btn, .logout-btn, a[href="/logout"]')) {
      e.preventDefault();
      logout();
    }
    if (e.target.matches('.approve-course')) {
      e.preventDefault();
      updateCourseStatus(e.target.dataset.id, 'APROVADO').catch(err => alert(err.message));
    }
    if (e.target.matches('.reject-course')) {
      e.preventDefault();
      updateCourseStatus(e.target.dataset.id, 'REAVALIAR').catch(err => alert(err.message));
    }
    if (e.target.matches('.view-user')) {
      e.preventDefault();
      alert('Edicao de acesso/logs ainda nao possui endpoint no backend.');
    }
  });

  document.addEventListener('input', e => {
    if (e.target.matches('.search-bar')) renderUsers();
  });
  document.addEventListener('change', e => {
    if (e.target.matches('.filter-select')) renderUsers();
  });

  const handleLoadingError = err => {
    if (renderCatalogError(err)) return;
    if (renderMonitoringError()) return;
    if (renderDashboardError(err)) return;

    const target = document.querySelector('main, .container');
    if (!target) return;
    const message = document.createElement('p');
    message.className = 'empty-state';
    message.textContent = err.message;
    target.prepend(message);
  };

  try {
    state.user = await window.jwtSession.recoverSession(API_BASE_URL, ['curador', 'admin'], '../index.html');
    const perfil = String(state.user?.perfil || state.user?.cargo || '').trim().toLowerCase();
    if (!state.user || !['curador', 'admin'].includes(perfil)) {
      window.location.href = '../index.html';
      return;
    }
    state.user.perfil = perfil;
    sessionStorage.setItem('session', JSON.stringify(state.user));
    updateIdentity();
    await refreshData();
  } catch (err) {
    if (!state.user) renderIdentityError();
    handleLoadingError(err);
  }
});
