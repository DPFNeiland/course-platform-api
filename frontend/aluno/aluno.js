const API_BASE_URL = window.APP_CONFIG.API_BASE_URL;

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
  const response = await window.jwtSession.authenticatedFetch(
    API_BASE_URL, path, { ...options, headers }, '../index.html'
  );
  if (response.status === 401) {
    throw new Error('Sessao expirada. Faca login novamente.');
  }
  if (!response.ok) {
    const error = await response.json().catch(() => null);
    throw new Error(error?.message || error?.error || error?.erro || 'Nao foi possivel concluir a operacao.');
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
  const courseId = Number(curso.id);
  const hasValidCourseId = Number.isInteger(courseId) && courseId > 0;
  const matricula = hasValidCourseId ? matriculaDoCurso(courseId) : null;
  const enrolled = Boolean(matricula);
  const finished = matricula?.status === 'ENCERRADA';
  const card = createTextElement('div', 'course-card card curso-card', '');
  const thumb = createTextElement('div', 'course-thumb curso-thumb', '');
  thumb.setAttribute('style', `background: ${gradients[index % gradients.length]};`);
  const badges = createTextElement('div', 'course-badges curso-info', '');
  badges.append(
    createTextElement('span', 'badge-pill', formatEnum(curso.tipoCurso)),
    createTextElement('span', 'badge-pill', formatEnum(curso.tipoAssinatura)),
    createTextElement('span', 'badge-pill', enrolled ? formatEnum(matricula.status) : 'Disponível')
  );
  const metadata = createTextElement('div', 'course-meta', '');
  const professorId = Number(curso.professorId);
  metadata.appendChild(createTextElement(
    'span',
    '',
    Number.isInteger(professorId) && professorId > 0 ? `Professor #${professorId}` : 'Professor indisponível'
  ));

  let action;
  if (finished) {
    action = createTextElement('a', 'btn-pill btn-secondary', 'Certificado');
    action.setAttribute('href', 'certificado.html');
  } else if (enrolled && hasValidCourseId) {
    action = createTextElement('a', 'btn-pill btn-primary', 'Continuar');
    action.setAttribute('href', `sala-aula.html?cursoId=${courseId}`);
  } else {
    action = createTextElement('button', 'btn-pill btn-primary enroll-course', hasValidCourseId ? 'Matricular' : 'Indisponível');
    action.setAttribute('type', 'button');
    if (hasValidCourseId) {
      action.dataset.courseId = String(courseId);
    } else {
      action.disabled = true;
      action.setAttribute('aria-disabled', 'true');
    }
  }

  card.append(
    thumb,
    createTextElement('h3', 'course-title', curso.nome || 'Curso sem nome'),
    createTextElement(
      'p',
      'course-desc',
      `Curso ${formatEnum(curso.tipoCurso).toLowerCase()} no plano ${formatEnum(curso.tipoAssinatura).toLowerCase()}.`
    ),
    badges,
    metadata,
    action
  );
  return card;
};

const renderCatalog = () => {
  document.querySelectorAll('[data-approved-courses]').forEach(container => {
    const limit = Number(container.dataset.limit || filteredCourses().length);
    const cursos = filteredCourses().slice(0, limit);
    const cards = cursos.map(createCourseCard);
    container.replaceChildren(...(cards.length
      ? cards
      : [createTextElement('p', 'empty-state', 'Nenhum curso aprovado disponível no momento.')]
    ));
    finishLoading(container);
  });
};

const renderStudentProgress = () => {
  const container = document.querySelector('[data-student-progress]');
  if (!container) return;
  const ativas = appState.matriculas.filter(m => m.status !== 'ENCERRADA');
  const cards = ativas
    .map((matricula, index) => {
      const curso = cursoPorId(matricula.cursoId);
      if (!curso) return null;
      return createCourseCard(curso, index);
    })
    .filter(Boolean);
  container.replaceChildren(...(cards.length
    ? cards
    : [createTextElement('p', 'empty-state', 'Nenhum curso iniciado ainda.')]
  ));
  finishLoading(container);
};

const provisionalGamificationPoints = () => {
  // TODO: Validar com Produto a regra definitiva de gamificação; 100 pontos por matrícula é uma regra provisória.
  return appState.matriculas.length * 100;
};

const renderDashboardStats = () => {
  const gamification = document.querySelector('[data-dashboard-gamification]');
  if (gamification) {
    gamification.textContent = 'Gamificação em breve.';
    finishLoading(gamification);
  }
  const availableCourses = document.querySelector('[data-dashboard-course-list]');
  const courseItems = appState.cursosAprovados.slice(0, 3).map(curso => {
    const li = document.createElement('li');
    li.append(
      createTextElement('span', '', curso.nome),
      createTextElement('span', 'badge badge-success', formatEnum(curso.status))
    );
    return li;
  });
  if (availableCourses) {
    availableCourses.replaceChildren(...(courseItems.length
      ? courseItems
      : [createTextElement('li', 'empty-state', 'Nenhum curso aprovado disponível.')]
    ));
    finishLoading(availableCourses.closest('.card'));
  }
  finishLoading(document.querySelector('.gamificacao'));
};

const renderStudentDashboardError = error => {
  const availableCourses = document.querySelector('[data-dashboard-course-list]');
  if (!availableCourses) return false;
  const message = error?.message || 'Dados do dashboard indisponíveis.';
  const gamification = document.querySelector('[data-dashboard-gamification]');
  if (gamification) {
    gamification.textContent = 'Gamificação indisponível.';
    finishLoading(gamification);
  }
  availableCourses.replaceChildren(createTextElement('li', 'dashboard-unavailable', message));
  finishLoading(availableCourses.closest('.card'));
  finishLoading(document.querySelector('.gamificacao'));
  return true;
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

const finishAvatarLoading = () => {
  document.querySelectorAll('.avatar').forEach(element => {
    element.classList.remove('avatar-skeleton');
    finishLoading(element);
  });
};

const createTextElement = (tagName, className, text) => {
  const element = document.createElement(tagName);
  element.className = className;
  element.textContent = text;
  return element;
};

const createAchievementCard = (icon, title, description, badgeText, badgeClass) => {
  const card = document.createElement('div');
  card.className = 'card';
  card.append(
    createTextElement('div', 'icon', icon),
    createTextElement('h3', '', title),
    createTextElement('p', '', description),
    createTextElement('span', `badge ${badgeClass}`, badgeText)
  );
  return card;
};

const renderAchievements = () => {
  const grid = document.querySelector('.achievements-grid');
  if (!grid) return;
  const finished = appState.matriculas.filter(m => m.status === 'ENCERRADA').length;
  const active = appState.matriculas.filter(m => m.status !== 'ENCERRADA').length;
  grid.replaceChildren(
    createAchievementCard('Cursos', `${appState.matriculas.length} matrícula(s)`, 'Total vindo do banco.', 'Matrículas', 'badge-aprendizado'),
    createAchievementCard('Ativos', `${active} em andamento`, 'Cursos que você iniciou.', 'Progresso', 'badge-progresso'),
    createAchievementCard('Concluídos', `${finished} encerrado(s)`, 'Cursos marcados como concluídos.', 'Certificados', 'badge-especial')
  );
  finishLoading(grid);

  const ranking = document.querySelector('.ranking');
  if (ranking) {
    const studentName = appState.session.nome || 'Aluno';
    const item = document.createElement('div');
    item.className = 'rank-item';
    item.append(
      createTextElement('div', 'rank-position', '—'),
      createTextElement('div', 'rank-avatar', studentName.slice(0, 2).toUpperCase()),
      createTextElement('div', 'rank-name', studentName),
      createTextElement('div', 'rank-score', `${provisionalGamificationPoints()} pts`)
    );
    ranking.replaceChildren(item);
  }
  finishLoading(document.querySelector('.ranking-section'));
};

const isAchievementsPage = () => Boolean(
  document.querySelector('.achievements-grid') && document.querySelector('.ranking')
);

const renderAchievementsError = error => {
  if (!isAchievementsPage()) return false;
  const message = error?.message || 'Nao foi possivel carregar suas conquistas.';
  const grid = document.querySelector('.achievements-grid');
  const ranking = document.querySelector('.ranking');
  grid.replaceChildren(createTextElement('p', 'empty-state', message));
  ranking.replaceChildren(createTextElement('p', 'empty-state', message));
  finishLoading(grid);
  finishLoading(document.querySelector('.ranking-section'));
  return true;
};

const createMaterialCard = (material, cursoId) => {
  const card = document.createElement('article');
  card.className = 'card material-card';
  const title = material.titulo || material.nomeArquivo || 'Material do curso';
  card.append(
    createTextElement('h4', '', title),
    createTextElement('p', '', formatEnum(material.tipo))
  );

  const link = createTextElement(material.tipo === 'ARQUIVO' ? 'button' : 'a', 'btn-pill btn-secondary', material.tipo === 'ARQUIVO' ? 'Baixar' : 'Acessar');
  if (material.tipo === 'ARQUIVO' && material.id != null) {
    link.type = 'button';
    link.className += ' download-material';
    link.dataset.downloadPath = `/curso/${cursoId}/materiais/${material.id}/arquivo`;
    link.dataset.fileName = material.nomeArquivo || 'material.bin';
  } else if (material.tipo === 'LINK' && /^https?:\/\//i.test(material.url || '')) {
    link.href = material.url;
    link.target = '_blank';
    link.rel = 'noopener noreferrer';
  } else {
    link.removeAttribute('href');
    link.className += ' disabled';
    link.textContent = 'Indisponível';
  }
  card.append(link);
  return card;
};

const renderMaterialsState = (container, message) => {
  container.replaceChildren(createTextElement('p', 'empty-state', message));
  finishLoading(container);
};

const isSalaAulaPage = () => Boolean(document.querySelector('.lesson-title'));

const renderSalaAulaError = error => {
  if (!isSalaAulaPage()) return false;
  const message = error?.message || 'Não foi possível carregar a sala de aula.';
  document.querySelectorAll('[data-room-course-title]').forEach(element => {
    element.textContent = 'Sala de aula indisponível';
    finishLoading(element);
  });

  const lessonTitle = document.querySelector('.lesson-title');
  if (lessonTitle) {
    lessonTitle.textContent = 'Não foi possível carregar o curso';
    finishLoading(lessonTitle);
  }
  const description = document.querySelector('.lesson-description');
  if (description) {
    description.replaceChildren(createTextElement('p', 'empty-state', message));
    finishLoading(description);
  }
  const progressText = document.querySelector('.progress-text');
  if (progressText) {
    progressText.textContent = 'Progresso indisponível';
    finishLoading(progressText);
  }
  const fill = document.querySelector('.lesson-content .progress-fill');
  if (fill) fill.style.width = '0%';
  const button = document.querySelector('.complete-course-btn');
  if (button) {
    button.textContent = 'Curso indisponível';
    button.disabled = true;
    delete button.dataset.completeMatricula;
    delete button.dataset.courseId;
  }
  const materials = document.querySelector('.support-materials .cards-grid');
  if (materials) renderMaterialsState(materials, message);
  return true;
};

const downloadFileName = (response, fallback) => {
  const disposition = response.headers?.get('Content-Disposition') || '';
  const match = disposition.match(/filename="?([^";]+)"?/i);
  if (!match) return fallback;
  try {
    return decodeURIComponent(match[1]);
  } catch {
    return match[1];
  }
};

const downloadMaterial = async button => {
  button.disabled = true;
  let objectUrl;
  try {
    const response = await window.jwtSession.authenticatedFetch(
      API_BASE_URL,
      button.dataset.downloadPath,
      { method: 'GET' },
      '../index.html'
    );
    if (response.status === 401) throw new Error('Sessão expirada. Faça login novamente.');
    if (!response.ok) {
      const error = await response.json().catch(() => null);
      throw new Error(error?.message || error?.error || error?.erro || 'Não foi possível baixar o material.');
    }

    objectUrl = URL.createObjectURL(await response.blob());
    const download = document.createElement('a');
    download.href = objectUrl;
    download.download = downloadFileName(response, button.dataset.fileName || 'material.bin');
    document.body.appendChild(download);
    download.click();
    download.remove();
  } catch (error) {
    alert(error?.message || 'Não foi possível baixar o material.');
  } finally {
    if (objectUrl) URL.revokeObjectURL(objectUrl);
    button.disabled = false;
  }
};

const renderSalaAula = async () => {
  const lessonTitle = document.querySelector('.lesson-title');
  if (!lessonTitle) return;
  const params = new URLSearchParams(window.location.search);
  const cursoIdSolicitado = Number(params.get('cursoId'));
  const matricula = cursoIdSolicitado
    ? matriculaDoCurso(cursoIdSolicitado)
    : appState.matriculas[0];
  const curso = matricula ? cursoPorId(matricula.cursoId) : null;
  const courseName = curso?.nome || 'Nenhum curso selecionado';

  document.querySelectorAll('[data-room-course-title]').forEach(element => {
    element.textContent = courseName;
    finishLoading(element);
  });
  lessonTitle.textContent = courseName;
  finishLoading(lessonTitle);

  const description = document.querySelector('.lesson-description');
  if (description) {
    description.replaceChildren(document.createTextNode(curso
      ? `Curso ${formatEnum(curso.tipoCurso)} no plano ${formatEnum(curso.tipoAssinatura)}.`
      : 'Matricule-se em um curso aprovado para acessar a sala de aula.'));
    finishLoading(description);
  }

  const finished = matricula?.status === 'ENCERRADA';
  const progressText = document.querySelector('.progress-text');
  if (progressText) {
    progressText.textContent = finished
      ? '100% concluído'
      : matricula
        ? `Progresso detalhado em breve — ${formatEnum(matricula.status)}`
        : 'Progresso indisponível';
    finishLoading(progressText);
  }
  const fill = document.querySelector('.lesson-content .progress-fill');
  if (fill) fill.style.width = finished ? '100%' : '0%';

  const button = document.querySelector('.complete-course-btn');
  if (button) {
    button.textContent = finished
      ? 'Curso concluído'
      : matricula
        ? 'Marcar curso como concluído'
        : 'Matrícula necessária';
    button.disabled = !matricula || finished;
    delete button.dataset.completeMatricula;
    delete button.dataset.courseId;
  }
  if (button && matricula) {
    button.dataset.completeMatricula = matricula.id;
    button.dataset.courseId = matricula.cursoId;
  }

  const comments = document.querySelector('.comments-list');
  if (comments) comments.replaceChildren(createTextElement('p', 'empty-state', 'Comentários em breve.'));

  const materials = document.querySelector('.support-materials .cards-grid');
  if (!materials) return;
  if (!curso || !matricula) {
    renderMaterialsState(materials, 'Matricule-se em um curso para acessar os materiais.');
    return;
  }

  try {
    const courseMaterials = await api(`/curso/${curso.id}/materiais`);
    if (!Array.isArray(courseMaterials)) throw new Error('Resposta de materiais inválida.');
    if (!courseMaterials.length) {
      renderMaterialsState(materials, 'Nenhum material disponível para este curso.');
      return;
    }
    materials.replaceChildren(...courseMaterials.map(material => createMaterialCard(material, curso.id)));
    finishLoading(materials);
  } catch (error) {
    renderMaterialsState(materials, error?.message || 'Não foi possível carregar os materiais.');
  }
};

const renderCertificates = () => {
  const finished = appState.matriculas.filter(m => m.status === 'ENCERRADA');
  const certificateItems = finished.map((matricula, index) => ({
    matricula,
    curso: cursoPorId(matricula.cursoId),
    gradient: `gradient-${(index % 6) + 1}`
  }));

  const stats = [
    ['#totalCursos', String(finished.length)],
    ['#totalHoras', 'Não disponível'],
    ['#mediaGeral', 'Não disponível']
  ];
  stats.forEach(([selector, value]) => {
    const element = document.querySelector(selector);
    if (!element) return;
    element.textContent = value;
    finishLoading(element);
  });
  finishLoading(document.querySelector('#statsSection'));

  document.querySelectorAll('.certificates-grid, .certificados-grid').forEach(container => {
    const cards = certificateItems.map(({ matricula, curso, gradient }) => {
      const card = createTextElement('article', 'certificado-card', '');
      const body = createTextElement('div', 'certificado-card-body', '');
      const info = createTextElement('div', 'certificado-card-info', '');
      const addInfo = (label, value) => {
        const item = createTextElement('div', 'certificado-card-info-item', '');
        item.append(
          createTextElement('span', 'certificado-card-info-label', label),
          createTextElement('span', 'certificado-card-info-value', value)
        );
        info.appendChild(item);
      };
      addInfo('Status:', formatEnum(matricula.status));
      addInfo('Concluído em:', matricula.dataMatricula || '-');
      addInfo('Professor:', curso?.professorId ? `#${curso.professorId}` : '-');
      body.append(
        createTextElement('div', 'certificado-card-title', curso?.nome || `Curso #${matricula.cursoId}`),
        info
      );

      const footer = createTextElement('div', 'certificado-card-footer', '');
      const courseId = Number(matricula.cursoId);
      const hasValidCourseId = Number.isInteger(courseId) && courseId > 0;
      const view = createTextElement('a', 'btn-visualizar', 'Visualizar');
      const share = createTextElement('button', 'btn-compartilhar', 'Compartilhar');
      share.setAttribute('type', 'button');
      if (hasValidCourseId) {
        view.setAttribute('href', `conclusao_certificado.html?cursoId=${courseId}`);
        share.dataset.shareCertificate = String(courseId);
      } else {
        view.setAttribute('aria-disabled', 'true');
        share.disabled = true;
        share.setAttribute('aria-disabled', 'true');
      }
      footer.append(view, share);
      card.append(
        createTextElement('div', `certificado-card-header ${gradient}`, 'CERT'),
        body,
        footer
      );
      return card;
    });

    if (cards.length) {
      container.replaceChildren(...cards);
      return;
    }
    const empty = createTextElement('div', 'empty-state', '');
    const explore = createTextElement('a', 'btn-comecal', 'Explorar Cursos');
    explore.setAttribute('href', 'catalogo.html');
    empty.append(
      createTextElement('div', 'empty-state-title', 'Nenhum certificado disponível'),
      createTextElement('div', 'empty-state-text', 'Conclua um curso para gerar certificado.'),
      explore
    );
    container.replaceChildren(empty);
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
  cursoEl.textContent = curso?.nome || 'Curso não concluído';
  const cargaHoraria = Number(curso?.cargaHoraria);
  const cargaHorariaEl = document.querySelector('#certificadoCargaHoraria');
  const dataEl = document.querySelector('#certificadoData');
  if (cargaHorariaEl) {
    cargaHorariaEl.textContent = Number.isFinite(cargaHoraria) && cargaHoraria > 0
      ? `${cargaHoraria} hora(s)`
      : 'Carga horária não disponível para este curso.';
    finishLoading(cargaHorariaEl);
  }
  if (dataEl) {
    dataEl.textContent = matricula?.dataMatricula || new Date().toLocaleDateString('pt-BR');
    finishLoading(dataEl);
  }
  finishLoading(nome);
  finishLoading(cursoEl);
};

const renderCertificatesError = error => {
  const statsSection = document.querySelector('#statsSection');
  const container = document.querySelector('#certificadosContainer');
  const nome = document.querySelector('#certificadoNome');
  if (!statsSection && !container && !nome) return false;

  ['#totalCursos', '#totalHoras', '#mediaGeral'].forEach(selector => {
    const element = document.querySelector(selector);
    if (!element) return;
    element.textContent = 'Indisponível';
    finishLoading(element);
  });
  finishLoading(statsSection);

  if (container) {
    container.replaceChildren(createTextElement(
      'p',
      'empty-state',
      error?.message || 'Não foi possível carregar os certificados.'
    ));
  }

  [
    ['#certificadoNome', appState.session?.nome || 'Aluno'],
    ['#certificadoCurso', 'Curso indisponível'],
    ['#certificadoCargaHoraria', 'Carga horária não disponível para este curso.'],
    ['#certificadoData', 'Data não disponível']
  ].forEach(([selector, value]) => {
    const element = document.querySelector(selector);
    if (!element) return;
    element.textContent = value;
    finishLoading(element);
  });
  return true;
};

const renderForum = () => {
  const discussions = document.querySelector('.discussions .container, .discussions');
  if (!discussions || document.querySelector('[data-approved-courses]')) return;

};

const refreshData = async () => {
  if (isAchievementsPage()) {
    const matriculas = await api('/matricula/me');
    if (!Array.isArray(matriculas)) throw new Error('Resposta de matrículas inválida.');
    appState.matriculas = matriculas.filter(m => Number(m.alunoId) === Number(appState.session.id));
    renderAchievements();
    return;
  }

  const [cursos, matriculas] = await Promise.all([
    api('/curso'),
    api('/matricula/me')
  ]);
  if (!Array.isArray(cursos)) throw new Error('Resposta de cursos inválida.');
  if (!Array.isArray(matriculas)) throw new Error('Resposta de matrículas inválida.');
  appState.cursos = cursos;
  appState.cursosAprovados = cursos.filter(c => c.status === 'APROVADO');
  appState.matriculas = matriculas.filter(m => Number(m.alunoId) === Number(appState.session.id));
  renderCatalog();
  renderStudentProgress();
  renderDashboardStats();
  renderAchievements();
  await renderSalaAula();
  renderCertificates();
  renderCertificateDetail();
  renderForum();
};

const handleClick = async event => {
  const materialDownload = event.target.closest('.download-material');
  if (materialDownload) {
    event.preventDefault();
    await downloadMaterial(materialDownload);
    return;
  }

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
    if (body) {
      body.textContent = `O LinkedIn será aberto em uma nova aba para você continuar o compartilhamento do certificado “${curso?.nome || 'curso concluído'}”.`;
    }
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
  try {
    appState.session = await window.jwtSession.recoverSession(API_BASE_URL, ['aluno'], '../index.html');
    if (!appState.session) {
      window.location.href = '../index.html';
      return;
    }

    document.querySelectorAll('[data-field="nomeAluno"]').forEach(el => {
      el.textContent = appState.session.nome || 'Aluno';
      finishLoading(el);
    });
    document.querySelectorAll('.avatar').forEach(el => {
      el.textContent = (appState.session.nome || 'AL').slice(0, 2).toUpperCase();
    });
    finishAvatarLoading();

    document.addEventListener('click', handleClick);
    document.addEventListener('input', event => {
      if (event.target.matches('.search-bar')) {
        appState.searchQuery = event.target.value;
        renderCatalog();
      }
    });

    await refreshData();
  } catch (err) {
    finishAvatarLoading();
    document.querySelectorAll('[data-field="nomeAluno"]').forEach(el => {
      if (!el.textContent.trim()) el.textContent = 'Nome indisponível';
      finishLoading(el);
    });
    if (renderAchievementsError(err)) return;
    if (renderSalaAulaError(err)) return;
    if (renderCertificatesError(err)) return;
    renderStudentDashboardError(err);
    document.querySelectorAll('[data-approved-courses], [data-student-progress]').forEach(container => {
      container.replaceChildren(createTextElement(
        'p',
        'empty-state',
        err?.message || 'Dados indisponíveis.'
      ));
      finishLoading(container);
    });
  }
});
