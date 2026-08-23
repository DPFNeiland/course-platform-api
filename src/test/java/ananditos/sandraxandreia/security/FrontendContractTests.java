package ananditos.sandraxandreia.security;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class FrontendContractTests {

    private final Path project = Path.of(System.getProperty("user.dir"));
    private final Path frontend = project.resolve("frontend");

    @Test
    void frontendNaoDeveExporJwtOuCredenciaisViaJavascript() throws IOException {
        try (Stream<Path> files = Files.walk(frontend)) {
            List<Path> scripts = files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".js"))
                    .filter(path -> !path.toString().endsWith(".test.js"))
                    .toList();

            assertThat(scripts).isNotEmpty();
            for (Path script : scripts) {
                String content = Files.readString(script);
                assertThat(content)
                        .as("conteudo de %s", script)
                        .doesNotContain("btoa(")
                        .doesNotContain("Authorization'] = `Basic")
                        .doesNotContain("Authorization\"] = `Basic")
                        .doesNotContain("Authorization: `Basic")
                        .doesNotContain("Authorization: `Bearer")
                        .doesNotContain("session.token");
            }
        }
    }

    @Test
    void clientesAutenticadosDevemCentralizarCookieECsrf() throws IOException {
        for (String relative : List.of("aluno/aluno.js", "curador/curador.js", "professor/professor.js")) {
            String content = Files.readString(frontend.resolve(relative));
            assertThat(content)
                    .as("cookie de sessao de %s", relative)
                    .contains("authenticatedFetch")
                    .doesNotContain("Bearer ${session.token}");
        }

        String session = Files.readString(frontend.resolve("session.js"));
        assertThat(session)
                .contains("credentials: 'include'")
                .contains("X-XSRF-TOKEN");

        String auth = Files.readString(frontend.resolve("auth.js"));
        assertThat(auth)
                .contains("jwtSession.csrfFetch")
                .contains("endpoint = '/curador'")
                .doesNotContain("fetch(`${API_BASE_URL}/login`")
                .doesNotContain("fetch(`${API_BASE_URL}${endpoint}`");

        String forum = Files.readString(frontend.resolve("aluno/forum.html"));
        assertThat(forum)
                .contains("<script src=\"../session.js\"></script>")
                .contains("<script src=\"aluno.js\"></script>")
                .doesNotContain("forum.js");
    }

    @Test
    void materiaisDoProfessorDevemEstarRemovidosDoProduto() throws IOException {
        Path professor = frontend.resolve("professor");

        assertThat(Files.exists(professor.resolve("materiais.html"))).isFalse();
        assertThat(Files.exists(professor.resolve("style/materiais.css"))).isFalse();

        try (Stream<Path> files = Files.walk(professor)) {
            for (Path file : files.filter(Files::isRegularFile).toList()) {
                assertThat(file.getFileName().toString().toLowerCase())
                        .as("arquivo de materiais remanescente em %s", file)
                        .doesNotContain("materiais");
                if (isProjectTextFile(file)) {
                    assertThat(Files.readString(file).toLowerCase())
                            .as("referência de materiais remanescente em %s", file)
                            .doesNotContain(
                                    "materiais.html",
                                    "materiais.css",
                                    "rendermateriais",
                                    ">materiais</a>");
                }
            }
        }
    }

    @Test
    void aulaAoVivoDoProfessorDeveEstarRemovidaDoProduto() throws IOException {
        Path professor = frontend.resolve("professor");

        assertThat(Files.exists(professor.resolve("aula-ao-vivo.html"))).isFalse();
        assertThat(Files.exists(professor.resolve("style/aula-ao-vivo.css"))).isFalse();

        try (Stream<Path> files = Files.walk(frontend)) {
            for (Path file : files
                    .filter(Files::isRegularFile)
                    .filter(this::isProjectTextFile)
                    .toList()) {
                assertThat(Files.readString(file).toLowerCase())
                        .as("link para aula ao vivo remanescente em %s", file)
                        .doesNotContain("aula-ao-vivo.html");
            }
        }

        try (Stream<Path> files = Files.walk(professor)) {
            for (Path file : files.filter(Files::isRegularFile).toList()) {
                assertThat(file.getFileName().toString().toLowerCase())
                        .as("arquivo de aula ao vivo remanescente em %s", file)
                        .doesNotContain("aula-ao-vivo");
                if (isProjectTextFile(file)) {
                    assertThat(Files.readString(file).toLowerCase())
                            .as("referência de aula ao vivo remanescente em %s", file)
                            .doesNotContain(
                                    "aula-ao-vivo.css",
                                    "renderaulaaovivo",
                                    "window.sendmessage",
                                    ".chat-input button",
                                    ">aula ao vivo</a>");
                }
            }
        }
    }

    @Test
    void frontendDeveCentralizarApiBaseUrlECarregarConfiguracaoAntesDosModulos() throws IOException {
        String localApiHardcode = "localhost:" + "8080";
        for (String relative : List.of(
                "auth.js", "aluno/aluno.js", "curador/curador.js", "professor/professor.js")) {
            assertThat(Files.readString(frontend.resolve(relative)))
                    .as("configuracao de API em %s", relative)
                    .contains("window.APP_CONFIG.API_BASE_URL")
                    .doesNotContain(localApiHardcode);
        }

        try (Stream<Path> files = Files.walk(project)) {
            for (Path file : files
                    .filter(Files::isRegularFile)
                    .filter(this::isProjectTextFile)
                    .filter(path -> !path.startsWith(project.resolve(".git")))
                    .filter(path -> !path.startsWith(project.resolve("decisoes")))
                    .filter(path -> !path.startsWith(project.resolve("target")))
                    .toList()) {
                if (file.equals(frontend.resolve("config.js"))) continue;
                assertThat(Files.readString(file))
                        .as("hardcode de API fora de config.js em %s", file)
                        .doesNotContain(localApiHardcode);
            }
        }

        try (Stream<Path> pages = Files.walk(frontend)) {
            for (Path page : pages
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".html"))
                    .toList()) {
                String html = Files.readString(page);
                if (!html.contains("session.js")) continue;
                assertThat(html.indexOf("config.js"))
                        .as("ordem de configuracao em %s", page)
                        .isGreaterThanOrEqualTo(0)
                        .isLessThan(html.indexOf("session.js"));
            }
        }
    }

    @Test
    void configuracaoPorAmbienteDeveEstarDocumentada() throws IOException {
        Path envExample = project.resolve(".env.example");

        assertThat(envExample).exists();
        assertThat(Files.readString(envExample))
                .contains("APP_ENV=development")
                .contains("API_BASE_URL=")
                .contains("APP_ENV=staging")
                .contains("APP_ENV=production")
                .contains("CORS_ALLOWED_ORIGINS")
                .contains("HTTPS");
    }

    @Test
    void catalogoDoCuradorDeveExibirSkeletonSemCursosFicticios() throws IOException {
        String html = Files.readString(frontend.resolve("curador/catalogo.html"));
        String script = Files.readString(frontend.resolve("curador/curador.js"));
        String styles = Files.readString(frontend.resolve("aluno/style/catalogo.css"));

        assertThat(html)
                .doesNotContain(
                        "JavaScript Moderno",
                        "Node.js & Express",
                        "Data Science com Python",
                        "React Avançado",
                        "Banco de Dados SQL",
                        "Habilidades de Comunicação")
                .contains("class=\"courses-grid\"")
                .contains("aria-busy=\"true\"")
                .contains("aria-label=\"Carregando cursos\"");
        assertThat(countOccurrences(html, "class=\"course-skeleton\""))
                .as("quantidade de skeletons do catalogo")
                .isEqualTo(4);

        assertThat(script)
                .contains("document.querySelector('.courses-grid, [data-curator-courses]')")
                .contains("container.removeAttribute('aria-busy')")
                .contains("container.replaceChildren(...cards)")
                .contains("element.textContent = text")
                .contains("message.textContent = error?.message")
                .contains("container.replaceChildren(message)")
                .doesNotContain("insertAdjacentHTML");
        assertThat(styles)
                .contains(".course-skeleton")
                .contains("@keyframes skeleton-pulse")
                .contains("prefers-reduced-motion: reduce");
    }

    @Test
    void monitoramentoDoCuradorNaoDeveExibirDadosFicticiosAntesDaCarga() throws IOException {
        String html = Files.readString(frontend.resolve("curador/monitoramento.html"));
        String script = Files.readString(frontend.resolve("curador/curador.js"));

        assertThat(html)
                .doesNotContain(
                        ">45<", ">12<", ">567<", ">2.345<",
                        "Atividade Recente", "Atividade Semanal",
                        "João Silva", "Maria Santos", "Pedro Oliveira", "Ana Costa", "Carlos Mendes",
                        "Matemática Básica", "Programação Web", "Inglês Avançado", "Design Gráfico", "Marketing Digital")
                .doesNotContain("<svg")
                .doesNotContain("Professores ativos", "Alunos ativos")
                .containsOnlyOnce("<div class=\"table-header\">Matrículas</div>")
                .contains("<div class=\"kpi-label\">Professores</div>")
                .contains("<div class=\"kpi-label\">Alunos</div>")
                .contains("class=\"kpis\" aria-busy=\"true\"")
                .contains("class=\"graphs\" aria-busy=\"true\"")
                .contains("class=\"table-section\" aria-busy=\"true\"");
        assertThat(countOccurrences(html, "<div class=\"kpi-number\">--</div>"))
                .as("quantidade de placeholders dos KPIs")
                .isEqualTo(4);

        assertThat(script)
                .contains("document.querySelectorAll('.kpi-number')")
                .contains("document.querySelector('.table-section tbody')")
                .contains("document.querySelector('.graphs')")
                .contains("async function refreshMonitoring()")
                .contains("Promise.allSettled")
                .contains("renderMonitoringError");
    }

    @Test
    void agendaDoProfessorDeveCarregarCalendarioAtualSemEventosFicticios() throws IOException {
        String html = Files.readString(frontend.resolve("professor/agenda.html"));
        String script = Files.readString(frontend.resolve("professor/professor.js"));

        assertThat(html)
                .doesNotContain(
                        "Novembro 2024",
                        "Reunião com pais do 3º ano",
                        "Atendimento individual - João S.",
                        "Aula ao vivo de Matemática",
                        "Avaliação de Ciências 2º ano")
                .doesNotContain("Hoje, 10:00", "Amanhã, 14:00", "Dia 20, 15:00", "Dia 23, 09:00")
                .contains("class=\"calendar-section\" aria-busy=\"true\"")
                .contains("class=\"events-section\" aria-busy=\"true\"")
                .contains("Carregando calendário atual...")
                .contains("Carregando cursos reais...")
                .contains("data-month-direction=\"-1\"")
                .contains("data-month-direction=\"1\"");

        assertThat(script)
                .contains("const renderCalendar = () =>")
                .contains("const renderAgendaCourses = () =>")
                .contains("agendaMonth.toLocaleDateString('pt-BR'")
                .contains("Number(curso.professorId) === Number(state.user.id)")
                .contains("api(`/curso/professor/${state.user.id}`)")
                .contains("renderAgendaError");
    }

    @Test
    void conquistasDoAlunoDevemExibirLoadingSemDadosFicticios() throws IOException {
        String html = Files.readString(frontend.resolve("aluno/conquistas.html"));
        String script = Files.readString(frontend.resolve("aluno/aluno.js"));
        String styles = Files.readString(frontend.resolve("aluno/style/conquistas.css"));

        assertThat(html)
                .doesNotContain(
                        "Primeiro Curso Concluído",
                        "Maratona de Estudos",
                        "Primeiro Curso",
                        "Top da Turma",
                        "Master Legendário",
                        "João Silva", "Maria Santos", "Pedro Costa", "Ana Souza", "Roberto Lima")
                .doesNotContain(">JD<")
                .contains("class=\"avatar avatar-skeleton\"")
                .contains("class=\"achievements-grid\" aria-busy=\"true\"")
                .contains("class=\"ranking-section\" aria-busy=\"true\"")
                .contains("class=\"rank-item ranking-skeleton\"");
        assertThat(countOccurrences(html, "class=\"card achievement-skeleton\""))
                .as("quantidade de skeletons das conquistas")
                .isEqualTo(3);

        assertThat(script)
                .contains("const provisionalGamificationPoints = () =>")
                .contains("TODO: Validar com Produto a regra definitiva de gamificação")
                .contains("const renderAchievementsError = error =>")
                .contains("const matriculas = await api('/matricula/me')")
                .doesNotContain("const matriculas = await api('/matricula')")
                .contains("grid.replaceChildren(")
                .contains("createTextElement('div', 'rank-position', '—')");
        assertThat(styles)
                .contains(".achievement-skeleton")
                .contains(".ranking-skeleton")
                .contains("@keyframes achievement-loading")
                .contains("prefers-reduced-motion: reduce")
                .doesNotContain(
                        ".badge-consistencia",
                        ".progress-bar",
                        ".progress-fill",
                        ".medal-gold",
                        ".medal-silver",
                        ".medal-bronze");
    }

    @Test
    void boletimSemEndpointDeNotasDevePermanecerRemovido() throws IOException {
        assertThat(frontend.resolve("aluno/boletim.html")).doesNotExist();
        assertThat(frontend.resolve("aluno/style/boletim.css")).doesNotExist();

        try (Stream<Path> files = Files.walk(frontend)) {
            List<Path> pages = files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".html"))
                    .toList();

            assertThat(pages).isNotEmpty();
            for (Path page : pages) {
                assertThat(Files.readString(page))
                        .as("navegacao de %s", frontend.relativize(page))
                        .doesNotContain("boletim.html");
            }
        }

        String alunoScript = Files.readString(frontend.resolve("aluno/aluno.js"));
        assertThat(alunoScript)
                .doesNotContain(
                        "renderBoletim",
                        "select-curso",
                        "notas-section",
                        "midia-final");

        try (Stream<Path> files = Files.walk(frontend.resolve("aluno"))) {
            List<Path> sources = files
                    .filter(Files::isRegularFile)
                    .filter(path -> Stream.of(".html", ".js", ".css")
                            .anyMatch(path.getFileName().toString()::endsWith))
                    .filter(path -> !path.getFileName().toString().endsWith(".test.js"))
                    .toList();

            assertThat(sources).isNotEmpty();
            for (Path source : sources) {
                assertThat(Files.readString(source))
                        .as("dados de boletim em %s", frontend.relativize(source))
                        .doesNotContain(
                                "React Avançado",
                                "Python para Data Science",
                                "Design de Interfaces",
                                "Node.js e Express",
                                "Média Final",
                                ">8.7<",
                                ">9.2<",
                                ">8.1<",
                                ">7.9<",
                                ">9.5<",
                                "class=\"nota\">-");
            }
        }
    }

    @Test
    void salaDeAulaDeveUsarCursoEMateriaisReaisSemMocks() throws IOException {
        String html = Files.readString(frontend.resolve("aluno/sala-aula.html"));
        String script = Files.readString(frontend.resolve("aluno/aluno.js"));
        String styles = Files.readString(frontend.resolve("aluno/style/sala-aula.css"));

        assertThat(html)
                .doesNotContain(
                        "Desenvolvimento Web Completo",
                        "Módulo 1: Introdução",
                        "Módulo 2: HTML/CSS",
                        "Módulo 3: JavaScript",
                        "Módulo 4: React",
                        "Módulo 5: Deploy",
                        "Fundamentos de CSS",
                        "45% concluído",
                        "width: 45%",
                        "Guia PDF",
                        "Quiz Interativo",
                        "João Silva",
                        "Maria Santos",
                        "comment-form")
                .contains(
                        "data-room-course-title",
                        "Módulos em breve.",
                        "Conteúdo das aulas em breve",
                        "class=\"card material-skeleton\"",
                        "Comentários em breve.");

        assertThat(script)
                .contains(
                        "const renderSalaAula = async () =>",
                        "const courseMaterials = await api(`/curso/${curso.id}/materiais`)",
                        "if (!Array.isArray(courseMaterials))",
                        "createMaterialCard(material, curso.id)",
                        "className += ' download-material'",
                        "window.jwtSession.authenticatedFetch(",
                        "const renderSalaAulaError = error =>",
                        "fill.style.width = finished ? '100%' : '0%'",
                        "await renderSalaAula();");

        assertThat(styles)
                .contains(
                        ".material-skeleton",
                        "@keyframes sala-loading",
                        "prefers-reduced-motion: reduce")
                .doesNotContain(
                        ".timeline-item",
                        ".video-player",
                        ".play-overlay",
                        ".comment-form",
                        ".comment-card");
    }

    @Test
    void dashboardsDevemCompartilharSkeletonSemDadosFicticios() throws IOException {
        String aluno = Files.readString(frontend.resolve("aluno/dashboard.html"));
        String curador = Files.readString(frontend.resolve("curador/dashboard.html"));
        String professor = Files.readString(frontend.resolve("professor/dashboard.html"));
        String globalStyles = Files.readString(frontend.resolve("global.css"));
        String dashboardUi = Files.readString(frontend.resolve("dashboard-ui.js"));

        assertThat(aluno)
                .doesNotContain(
                        ">JD<", "1.250", "2.000", "🥇 Ouro", "🥈 Prata",
                        "Cursos Bônus", "Curso de JavaScript Básico", "Design Thinking", "Produtividade com Notion",
                        "width: 62.5%", "points-current", "points-total")
                .contains(
                        "data-field=\"nomeAluno\"",
                        "loading-skeleton loading-skeleton-text",
                        "data-dashboard-course-list",
                        "data-dashboard-gamification",
                        "../dashboard-ui.js",
                        ">Mudar Plano</button>",
                        "disabled",
                        "aria-disabled=\"true\"");

        assertThat(curador)
                .doesNotContain(">JD<", "Olá, Fulano!")
                .contains(
                        "data-field=\"nomeCurador\"",
                        "data-curator-dashboard-summary",
                        "../dashboard-ui.js");

        assertThat(professor)
                .doesNotContain(
                        ">JD<", ">92%<", ">87%<",
                        "Avaliações pendentes", "Aulas ministradas no mês",
                        "Nova avaliação criada", "Aluno Maria entregou tarefa", "Atividade 3.2 - Física",
                        "Resposta no fórum", "Revisão geral - Turma B", "Slides da aula 5 enviados")
                .doesNotContain("<svg", "polyline points=")
                .contains(
                        "data-field=\"nomeProfessor\"",
                        "class=\"kpi-grid\"",
                        "class=\"activities-grid\"",
                        "<h2>Cursos cadastrados</h2>",
                        "../dashboard-ui.js")
                .doesNotContain("<h2>Atividades Recentes</h2>");

        for (String dashboard : List.of(aluno, curador, professor)) {
            assertThat(dashboard)
                    .contains("loading-skeleton")
                    .contains("loading-skeleton-avatar");
        }

        assertThat(globalStyles)
                .contains(
                        ".loading-skeleton",
                        ".loading-skeleton-text",
                        ".loading-skeleton-avatar",
                        ".loading-skeleton-card",
                        "@keyframes dashboard-skeleton-loading",
                        "prefers-reduced-motion: reduce");

        assertThat(dashboardUi)
                .contains(
                        "global.dashboardUI = Object.freeze({ finishLoading })",
                        "element.removeAttribute('aria-hidden')",
                        "'loading-skeleton-avatar'");

        assertThat(Files.readString(frontend.resolve("aluno/aluno.js")))
                .contains("const renderDashboardStats = () =>")
                .contains("const renderStudentDashboardError = error =>")
                .contains("Gamificação em breve.")
                .contains("finishLoading(document.querySelector('.gamificacao'))")
                .contains(
                        "const hasValidCourseId = Number.isInteger(courseId) && courseId > 0",
                        "container.replaceChildren",
                        "action.dataset.courseId = String(courseId)")
                .doesNotContain("container.innerHTML = `<p class=\"empty-state\">${err.message}</p>`");
        assertThat(Files.readString(frontend.resolve("curador/curador.js")))
                .contains("document.querySelectorAll('[data-field=\"nomeCurador\"]')")
                .contains("const renderDashboardError = error =>");
        assertThat(Files.readString(frontend.resolve("professor/professor.js")))
                .contains("const renderDashboard = () =>")
                .contains("const renderDashboardError = error =>")
                .contains("activities.replaceChildren")
                .contains("charts.replaceChildren");
    }

    @Test
    void perfilECertificadosDoAlunoNaoDevemExibirDadosFicticiosDuranteCarregamento() throws IOException {
        String catalogo = Files.readString(frontend.resolve("aluno/catalogo.html"));
        String certificados = Files.readString(frontend.resolve("aluno/certificado.html"));
        String conclusao = Files.readString(frontend.resolve("aluno/conclusao_certificado.html"));
        String alunoJs = Files.readString(frontend.resolve("aluno/aluno.js"));
        String certificadoStyles = Files.readString(frontend.resolve("aluno/style/certificado.css"));

        for (String html : List.of(catalogo, certificados, conclusao)) {
            assertThat(html)
                    .doesNotContain("<div class=\"avatar\">JD</div>", "<div class=\"avatar\">AL</div>")
                    .contains(
                            "avatar loading-skeleton loading-skeleton-avatar",
                            "aria-label=\"Carregando perfil\"",
                            "../dashboard-ui.js");
        }

        assertThat(certificados)
                .doesNotContain(
                        "id=\"totalCursos\">0</div>",
                        "id=\"totalHoras\">-</div>",
                        "id=\"mediaGeral\">-</div>")
                .contains(
                        "id=\"statsSection\" aria-busy=\"true\"",
                        "id=\"totalCursos\" aria-hidden=\"true\"",
                        "id=\"totalHoras\" aria-hidden=\"true\"",
                        "id=\"mediaGeral\" aria-hidden=\"true\"",
                        "O LinkedIn será aberto em uma nova aba");

        assertThat(conclusao)
                .doesNotContain("Nao informada pelo backend", "Não informada pelo backend")
                .contains(
                        "id=\"certificadoCargaHoraria\" class=\"loading-skeleton loading-skeleton-text\"",
                        "id=\"certificadoData\" class=\"loading-skeleton loading-skeleton-text\"");

        assertThat(alunoJs)
                .contains(
                        "element.textContent = value",
                        "finishLoading(document.querySelector('#statsSection'))",
                        "const getInitials = name =>",
                        "if (!parts.length) return '?'",
                        "getInitials(appState.session.nome)",
                        "const rawCourseId = params.get('cursoId') ?? params.get('id')",
                        "Number.isInteger(cursoId)",
                        "Certificado não encontrado. Verifique o endereço",
                        "const refreshCertificatesData = async () =>",
                        "renderCertificateStats()",
                        "matricula.dataConclusao || 'Data de conclusão não disponível'",
                        "matricula?.dataEmissao",
                        "|| matricula?.dataConclusao",
                        "Carga horária não disponível para este curso.",
                        "body.textContent = `O LinkedIn será aberto em uma nova aba")
                .doesNotContain(
                        "body.innerHTML = `Voce esta prestes a compartilhar",
                        "if (!parts.length) return 'AL'",
                        "addInfo('Concluído em:', matricula.dataMatricula",
                        "dataEl.textContent = matricula?.dataMatricula",
                        "Nao informada pelo backend");

        assertThat(certificadoStyles)
                .contains(
                        ".certificados-grid > .empty-state",
                        "grid-column: 1 / -1");
    }

    private long countOccurrences(String content, String fragment) {
        return content.split(java.util.regex.Pattern.quote(fragment), -1).length - 1L;
    }

    private boolean isProjectTextFile(Path path) {
        String name = path.getFileName().toString();
        if (name.equals("Dockerfile") || name.equals(".gitignore") || name.startsWith("README")) {
            return true;
        }
        return Stream.of(
                        ".css", ".example", ".html", ".java", ".js", ".json", ".md",
                        ".properties", ".sh", ".sql", ".txt", ".xml", ".yaml", ".yml")
                .anyMatch(name::endsWith);
    }
}
