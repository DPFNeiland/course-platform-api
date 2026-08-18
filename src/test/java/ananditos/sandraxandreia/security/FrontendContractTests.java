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
