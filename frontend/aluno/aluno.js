const API_BASE_URL = 'http://localhost:8080';

const appState = {
  session: null,
  cursos: [],
  cursosAprovados: [],
  matriculas: [],
  currentFilter: 'todos',
  searchQuery: ''
};

const api = async (path, options = {}) => {
  const session = getSession();
  if (!session) throw new Error('Sessao expirada. Faca login novamente.');
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
  const requestOptions = await window.jwtSession.authenticatedOptions(API_BASE_URL, { ...options, headers });
  const response = await fetch(`${API_BASE_URL}${path}`, requestOptions);
  if (await window.jwtSession.handleUnauthorized(response, '../index.html')) {
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

const getSession = () => {
  return window.jwtSession.requireSession(['aluno'], '../index.html');
};

const logout = () => {
  window.jwtSession.logout(API_BASE_URL, '../index.html');
};

const matriculaDoCurso = cursoId => appState.matriculas.find(m => Number(m.cursoId) === Number(cursoId));
const cursoPorId = id => appState.cursos.find(c => Number(c.id) === Number(id));

const filteredCourses = () => {
  const filter = appState.currentFilter;
  const query = appState.searchQuery.trim().toLowerCase();
  return appState.cursosAprovados.filter(curso => {
    const matchesFilter = filter === 'todos'
      || formatEnum(curso.tipoCurso).toLowerCase().includes(filter)
      || formatEnum(curso.tipoAssinatura).toLowerCase().includes(filter);
    const matchesQuery = !query || curso.nome.toLowerCase().includes(query);
    return matchesFilter && matchesQuery;
  });
};

const createCourseCard = (curso, index) => {
  const gradients = [
    'linear-gradient(135deg, #1E40AF, #F97316)',
    'linear-gradient(135deg, #10B981, #2563EB)',
    'linear-gradient(135deg, #F59E0B, #10B981)',
    'linear-gradient(135deg, #1E3A8A, #EF4444)'
  ];
  const matricula = matriculaDoCurso(curso.id);
  const enrolled = Boolean(matricula);
  const finished = matricula?.status === 'ENCERRADA';
  const action = finished
    ? `<a class="btn-pill btn-secondary" href="certificado.html">Certificado</a>`
    : enrolled
      ? `<a class="btn-pill btn-primary" href="sala-aula.html?cursoId=${curso.id}">Continuar</a>`
      : `<button class="btn-pill btn-primary enroll-course" data-course-id="${curso.id}">Matricular</button>`;

  return `
    <div class="course-card card curso-card">
      <div class="course-thumb curso-thumb" style="background: ${gradients[index % gradients.length]};"></div>
      <h3 class="course-title">${curso.nome}</h3>
      <p class="course-desc">Curso ${formatEnum(curso.tipoCurso).toLowerCase()} no plano ${formatEnum(curso.tipoAssinatura).toLowerCase()}.</p>
      <div class="course-badges curso-info">
        <span class="badge-pill">${formatEnum(curso.tipoCurso)}</span>
        <span class="badge-pill">${formatEnum(curso.tipoAssinatura)}</span>
        <span class="badge-pill">${enrolled ? formatEnum(matricula.status) : 'Disponivel'}</span>
      </div>
      <div class="course-meta"><span>Professor #${curso.professorId}</span></div>
      ${action}
    </div>
  `;
};

const renderCatalog = () => {
  document.querySelectorAll('[data-approved-courses]').forEach(container => {
    const limit = Number(container.dataset.limit || filteredCourses().length);
    const cursos = filteredCourses().slice(0, limit);
    container.innerHTML = cursos.length
      ? cursos.map(createCourseCard).join('')
      : '<p class="empty-state">Nenhum curso aprovado disponivel no momento.</p>';
  });
};

const renderStudentProgress = () => {
  const container = document.querySelector('[data-student-progress]');
  if (!container) return;
  const ativas = appState.matriculas.filter(m => m.status !== 'ENCERRADA');
  container.innerHTML = ativas.length
    ? ativas.map((matricula, index) => {
      const curso = cursoPorId(matricula.cursoId);
      if (!curso) return '';
      return createCourseCard(curso, index);
    }).join('')
    : '<p class="empty-state">Nenhum curso iniciado ainda.</p>';
};

const renderDashboardStats = () => {
  const active = appState.matriculas.filter(m => m.status !== 'ENCERRADA').length;
  const finished = appState.matriculas.filter(m => m.status === 'ENCERRADA').length;
  document.querySelector('.points-current')?.replaceChildren(document.createTextNode(String((active + finished) * 100)));
  document.querySelector('.points-total')?.replaceChildren(document.createTextNode('/ pontos reais'));
  document.querySelectorAll('.achievements .badge').forEach((badge, index) => {
    badge.textContent = index === 0 ? `${finished} concluidos` : `${active} ativos`;
  });
  document.querySelector('.bonus-list')?.replaceChildren(...(appState.cursosAprovados.slice(0, 3).map(curso => {
    const li = document.createElement('li');
    li.innerHTML = `<span>${curso.nome}</span><span class="badge badge-success">${formatEnum(curso.status)}</span>`;
    return li;
  })));
};

const renderAchievements = () => {
  const grid = document.querySelector('.achievements-grid');
  if (!grid) return;
  const finished = appState.matriculas.filter(m => m.status === 'ENCERRADA').length;
  const active = appState.matriculas.filter(m => m.status !== 'ENCERRADA').length;
  grid.innerHTML = `
    <div class="card"><div class="icon">Cursos</div><h3>${appState.matriculas.length} matricula(s)</h3><p>Total vindo do banco.</p><span class="badge badge-aprendizado">Matriculas</span></div>
    <div class="card"><div class="icon">Ativos</div><h3>${active} em andamento</h3><p>Cursos que voce iniciou.</p><span class="badge badge-progresso">Progresso</span></div>
    <div class="card"><div class="icon">Concluídos</div><h3>${finished} encerrado(s)</h3><p>Cursos marcados como concluidos.</p><span class="badge badge-especial">Certificados</span></div>
  `;
  const ranking = document.querySelector('.ranking');
  if (ranking) {
    ranking.innerHTML = `<div class="rank-item"><div class="rank-position">1</div><div class="rank-avatar">${appState.session.nome?.slice(0, 2).toUpperCase() || 'AL'}</div><div class="rank-name">${appState.session.nome}</div><div class="rank-score">${appState.matriculas.length * 100} pts</div></div>`;
  }
};

const renderSalaAula = () => {
  const lessonTitle = document.querySelector('.lesson-title');
  if (!lessonTitle) return;
  const params = new URLSearchParams(window.location.search);
  const curso = cursoPorId(params.get('cursoId')) || cursoPorId(appState.matriculas[0]?.cursoId);
  const matricula = curso ? matriculaDoCurso(curso.id) : null;
  lessonTitle.textContent = curso?.nome || 'Nenhum curso selecionado';
  document.querySelector('.lesson-description')?.replaceChildren(document.createTextNode(curso
    ? `Curso ${formatEnum(curso.tipoCurso)} no plano ${formatEnum(curso.tipoAssinatura)}.`
    : 'Matricule-se em um curso aprovado para acessar a sala de aula.'));
  document.querySelector('.progress-text')?.replaceChildren(document.createTextNode(matricula?.status === 'ENCERRADA' ? '100% concluido' : 'Em andamento'));
  const fill = document.querySelector('.lesson-content .progress-fill');
  if (fill) fill.style.width = matricula?.status === 'ENCERRADA' ? '100%' : '25%';
  const button = document.querySelector('.lesson-content .btn-primary');
  if (button && matricula) {
    button.textContent = matricula.status === 'ENCERRADA' ? 'Curso concluido' : 'Marcar como Concluido';
    button.dataset.completeMatricula = matricula.id;
    button.dataset.courseId = matricula.cursoId;
    button.disabled = matricula.status === 'ENCERRADA';
  }
  const comments = document.querySelector('.comments-list');
  if (comments) comments.innerHTML = '<p class="empty-state">Forum e comentarios ainda nao possuem endpoint no backend.</p>';
  const materials = document.querySelector('.support-materials .cards-grid');
  if (materials) materials.innerHTML = '<p class="empty-state">Materiais ainda nao possuem endpoint no backend.</p>';
};

const renderCertificates = () => {
  const finished = appState.matriculas.filter(m => m.status === 'ENCERRADA');
  const certificateItems = finished.map((matricula, index) => ({
    matricula,
    curso: cursoPorId(matricula.cursoId),
    gradient: `gradient-${(index % 6) + 1}`
  }));

  document.querySelector('#totalCursos')?.replaceChildren(document.createTextNode(String(finished.length)));
  document.querySelector('#totalHoras')?.replaceChildren(document.createTextNode('-'));
  document.querySelector('#mediaGeral')?.replaceChildren(document.createTextNode('-'));

  document.querySelectorAll('.certificates-grid, .certificados-grid').forEach(container => {
    container.innerHTML = certificateItems.length
      ? certificateItems.map(({ matricula, curso, gradient }) => `
        <article class="certificado-card">
          <div class="certificado-card-header ${gradient}">CERT</div>
          <div class="certificado-card-body">
            <div class="certificado-card-title">${curso?.nome || `Curso #${matricula.cursoId}`}</div>
            <div class="certificado-card-info">
              <div class="certificado-card-info-item">
                <span class="certificado-card-info-label">Status:</span>
                <span class="certificado-card-info-value">${formatEnum(matricula.status)}</span>
              </div>
              <div class="certificado-card-info-item">
                <span class="certificado-card-info-label">Concluido em:</span>
                <span class="certificado-card-info-value">${matricula.dataMatricula || '-'}</span>
              </div>
              <div class="certificado-card-info-item">
                <span class="certificado-card-info-label">Professor:</span>
                <span class="certificado-card-info-value">#${curso?.professorId || '-'}</span>
              </div>
            </div>
          </div>
          <div class="certificado-card-footer">
            <a href="conclusao_certificado.html?cursoId=${matricula.cursoId}" class="btn-visualizar">Visualizar</a>
            <button class="btn-compartilhar" type="button" data-share-certificate="${matricula.cursoId}">Compartilhar</button>
          </div>
        </article>
      `).join('')
      : '<div style="grid-column: 1 / -1;"><div class="empty-state"><div class="empty-state-title">Nenhum certificado disponivel</div><div class="empty-state-text">Conclua um curso para gerar certificado.</div><a class="btn-comecal" href="catalogo.html">Explorar Cursos</a></div></div>';
  });
};

const renderCertificateDetail = () => {
  const nome = document.querySelector('#certificadoNome');
  const cursoEl = document.querySelector('#certificadoCurso');
  if (!nome || !cursoEl) return;
  const params = new URLSearchParams(window.location.search);
  const cursoId = Number(params.get('cursoId') || params.get('id'));
  const matricula = appState.matriculas.find(m => m.status === 'ENCERRADA' && (!cursoId || Number(m.cursoId) === cursoId));
  const curso = matricula ? cursoPorId(matricula.cursoId) : null;
  nome.textContent = appState.session.nome || 'Aluno';
  cursoEl.textContent = curso?.nome || 'Curso nao concluido';
  document.querySelector('#certificadoCargaHoraria')?.replaceChildren(document.createTextNode(curso ? 'Nao informada pelo backend' : '-'));
  document.querySelector('#certificadoData')?.replaceChildren(document.createTextNode(matricula?.dataMatricula || new Date().toLocaleDateString('pt-BR')));
};

const renderForum = () => {
  const discussions = document.querySelector('.discussions .container, .discussions');
  if (!discussions || document.querySelector('[data-approved-courses]')) return;

};

const refreshData = async () => {
  const [cursos, matriculas] = await Promise.all([
    api('/curso'),
    api('/matricula')
  ]);
  appState.cursos = cursos;
  appState.cursosAprovados = cursos.filter(c => c.status === 'APROVADO');
  appState.matriculas = matriculas.filter(m => Number(m.alunoId) === Number(appState.session.id));
  renderCatalog();
  renderStudentProgress();
  renderDashboardStats();
  renderAchievements();
  renderSalaAula();
  renderCertificates();
  renderCertificateDetail();
  renderForum();
};

const handleClick = async event => {
  const enroll = event.target.closest('.enroll-course');
  if (enroll) {
    event.preventDefault();
    enroll.disabled = true;
    try {
      await api('/matricula', {
        method: 'POST',
        body: JSON.stringify({ alunoId: appState.session.id, cursoId: Number(enroll.dataset.courseId), status: 'ATIVA' })
      });
      await refreshData();
    } catch (err) {
      alert(err.message);
      enroll.disabled = false;
    }
    return;
  }

  const complete = event.target.closest('[data-complete-matricula]');
  if (complete) {
    event.preventDefault();
    await api(`/matricula/${complete.dataset.completeMatricula}`, {
      method: 'PUT',
      body: JSON.stringify({ alunoId: appState.session.id, cursoId: Number(complete.dataset.courseId), status: 'ENCERRADA' })
    }).catch(err => alert(err.message));
    await refreshData();
    return;
  }

  const filter = event.target.closest('.filter-pill, .filter-btn, .filtro');
  if (filter) {
    event.preventDefault();
    document.querySelectorAll('.filter-pill, .filter-btn, .filtro').forEach(btn => btn.classList.remove('active'));
    filter.classList.add('active');
    appState.currentFilter = filter.textContent.trim().toLowerCase();
    if (appState.currentFilter === 'todos' || appState.currentFilter === 'recomendados') appState.currentFilter = 'todos';
    renderCatalog();
  }

  if (event.target.matches('.logout-btn')) logout();

  const share = event.target.closest('[data-share-certificate]');
  if (share) {
    const curso = cursoPorId(Number(share.dataset.shareCertificate));
    const modal = document.querySelector('#modalCompartilhar');
    const body = document.querySelector('#modalCompartilharBody');
    if (body) body.innerHTML = `Voce esta prestes a compartilhar o certificado de "<strong>${curso?.nome || 'curso concluido'}</strong>" no LinkedIn.`;
    if (modal) modal.style.display = 'block';
  }

  if (event.target.matches('[data-close-certificate-modal]')) {
    document.querySelector('#modalCompartilhar').style.display = 'none';
  }

  if (event.target.matches('[data-confirm-certificate-share]')) {
    window.open('https://www.linkedin.com/feed/?linkOrigin=LI_BADGE', '_blank');
    document.querySelector('#modalCompartilhar').style.display = 'none';
  }
};

document.addEventListener('DOMContentLoaded', async () => {
  appState.session = getSession();
  if (!appState.session) {
    window.location.href = '../index.html';
    return;
  }

  document.querySelectorAll('[data-field="nomeAluno"]').forEach(el => {
    el.textContent = appState.session.nome || 'Aluno';
  });
  document.querySelectorAll('.avatar').forEach(el => {
    el.textContent = (appState.session.nome || 'AL').slice(0, 2).toUpperCase();
  });

  document.addEventListener('click', handleClick);
  document.addEventListener('input', event => {
    if (event.target.matches('.search-bar')) {
      appState.searchQuery = event.target.value;
      renderCatalog();
    }
  });

  try {
    await refreshData();
  } catch (err) {
    document.querySelectorAll('[data-approved-courses], [data-student-progress]').forEach(container => {
      container.innerHTML = `<p class="empty-state">${err.message}</p>`;
    });
  }
});
