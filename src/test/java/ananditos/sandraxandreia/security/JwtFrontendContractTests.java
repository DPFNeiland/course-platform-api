package ananditos.sandraxandreia.security;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class JwtFrontendContractTests {

    private final Path frontend = Path.of(System.getProperty("user.dir"), "frontend");

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
    void clientesAutenticadosDevemEnviarCookieHttpOnly() throws IOException {
        for (String relative : List.of("aluno/aluno.js", "curador/curador.js", "professor/professor.js")) {
            String content = Files.readString(frontend.resolve(relative));
            assertThat(content)
                    .as("cookie de sessao de %s", relative)
                    .contains("credentials: 'include'")
                    .doesNotContain("Bearer ${session.token}");
        }

        String auth = Files.readString(frontend.resolve("auth.js"));
        assertThat(auth).contains("credentials: 'include'");
    }
}
