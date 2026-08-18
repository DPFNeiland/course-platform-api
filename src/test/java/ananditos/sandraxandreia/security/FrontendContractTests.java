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
