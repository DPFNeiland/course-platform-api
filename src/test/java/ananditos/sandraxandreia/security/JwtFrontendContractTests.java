package ananditos.sandraxandreia.security;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtFrontendContractTests {

    private final Path frontend = Path.of(System.getProperty("user.dir"), "frontend");

    @Test
    void frontendNaoDeveArmazenarOuEnviarTokenViaJavascript() throws IOException {
        List<Path> scripts = List.of(
                frontend.resolve("auth.js"),
                frontend.resolve("aluno/aluno.js"),
                frontend.resolve("curador/curador.js"),
                frontend.resolve("professor/professor.js"),
                frontend.resolve("session.js")
        );

        for (Path script : scripts) {
            String content = Files.readString(script);
            assertThat(content)
                    .as("conteudo de %s", script)
                    .doesNotContain("btoa(")
                    .doesNotContain("Authorization'] = `Basic")
                    .doesNotContain("Authorization\"] = `Basic")
                    .doesNotContain("Authorization'] = `Bearer")
                    .doesNotContain("session.token");
        }
    }

    @Test
    void clientesAutenticadosDevemEnviarCookieComCredentialsInclude() throws IOException {
        for (String relative : List.of("aluno/aluno.js", "curador/curador.js", "professor/professor.js")) {
            String content = Files.readString(frontend.resolve(relative));
            assertThat(content)
                    .as("cookie HttpOnly de %s", relative)
                    .contains("credentials: 'include'")
                    .doesNotContain("Bearer ${session.token}");
        }
    }
}
