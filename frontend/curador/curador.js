document.addEventListener('DOMContentLoaded', async function() {
  const API_BASE_URL = 'http://localhost:8080';
  const state = { user: null, cursos: [], matriculas: [], alunos: [], professores: [], usuarios: [] };

  const api = async (path, options = {}) => {
    const response = await fetch(`${API_BASE_URL}${path}`, {
      headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
      ...options
    });
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

  const userStr = sessionStorage.getItem('user') || sessionStorage.getItem('session');
  try {
    state.user = userStr ? JSON.parse(userStr) : null;
  } catch {
    state.user = null;
  }

  const perfil = String(state.user?.perfil || state.user?.cargo || '').trim().toLowerCase();
  if (!state.user || !['curador', 'admin'].includes(perfil)) {
    window.location.href = '../index.html';
    return;
  }
  state.user.perfil = perfil;
  sessionStorage.setItem('user', JSON.stringify(state.user));
  sessionStorage.setItem('session', JSON.stringify(state.user));

  const cursoPorId = id => state.cursos.find(curso => Number(curso.id) === Number(id));
  const alunoPorId = id => state.alunos.find(aluno => Number(aluno.id) === Number(id));
  const professorPorId = id => state.professores.find(prof => Number(prof.id) === Number(id));

  const updateIdentity = () => {
    document.querySelectorAll('.avatar').forEach(el => {
      el.textContent = (state.user.nome || 'CU').slice(0, 2).toUpperCase();
    });
    const greeting = document.querySelector('.header h1');
    if (greeting) greeting.textContent = `Ola, ${state.user.nome || 'Curador'}!`;
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
    const container = document.querySelector('.courses-grid, [data-curator-courses]');
    if (!container || document.querySelector('[data-pending-courses]')) return;
    container.innerHTML = state.cursos.length
      ? state.cursos.map(curso => `
        <article class="course-card">
          <div class="course-thumb"></div>
          <h3 class="course-title">${curso.nome}</h3>
          <p class="course-desc">${formatEnum(curso.tipoCurso)} - ${formatEnum(curso.tipoAssinatura)}</p>
          <div class="course-badges">
            <span class="badge-pill">${formatEnum(curso.status)}</span>
            <span class="badge-pill">${professorPorId(curso.professorId)?.nome || `Professor #${curso.professorId}`}</span>
          </div>
          <div class="actions">
            <button class="btn approve-course" data-id="${curso.id}">Aprovar</button>
            <button class="btn reject-course" data-id="${curso.id}">Reavaliar</button>
          </div>
        </article>
      `).join('')
      : '<p class="empty-state">Nenhum curso cadastrado.</p>';
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
          card.querySelector('p').textContent = `${count} registro(s) reais no banco.`;
        }
      });
    }
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
    const kpis = document.querySelectorAll('.kpi-number');
    if (kpis.length) {
      kpis[0].textContent = state.cursos.filter(c => c.status === 'APROVADO').length;
      kpis[1].textContent = state.professores.length;
      kpis[2].textContent = state.alunos.length;
      kpis[3].textContent = state.matriculas.length;
    }
    const tbody = document.querySelector('.table-section tbody');
    if (tbody) {
      tbody.innerHTML = state.matriculas.length
        ? state.matriculas.map(matricula => {
          const curso = cursoPorId(matricula.cursoId);
          const aluno = alunoPorId(matricula.alunoId);
          return `
            <tr>
              <td>${curso?.nome || `Curso #${matricula.cursoId}`}</td>
              <td>${aluno?.nome || `Aluno #${matricula.alunoId}`}</td>
              <td>${formatEnum(matricula.status)}</td>
              <td>${matricula.dataMatricula || '-'}</td>
            </tr>
          `;
        }).join('')
        : '<tr><td colspan="4">Nenhuma matricula registrada.</td></tr>';
    }
    const graphs = document.querySelector('.graphs');
    if (graphs) {
      graphs.innerHTML = `
        <div class="graph-card"><h3>Cursos por status</h3><p>Aprovados: ${state.cursos.filter(c => c.status === 'APROVADO').length}</p><p>Em avaliacao: ${state.cursos.filter(c => c.status === 'EM_AVALIACAO').length}</p><p>Reavaliar: ${state.cursos.filter(c => c.status === 'REAVALIAR').length}</p></div>
        <div class="graph-card"><h3>Matriculas</h3><p>${state.matriculas.length} registro(s) reais no banco.</p></div>
      `;
    }
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

  async function refreshData() {
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
    sessionStorage.clear();
    window.location.href = '../index.html';
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

  updateIdentity();
  await refreshData().catch(err => {
    document.querySelector('main, .container')?.insertAdjacentHTML('afterbegin', `<p class="empty-state">${err.message}</p>`);
  });
});
