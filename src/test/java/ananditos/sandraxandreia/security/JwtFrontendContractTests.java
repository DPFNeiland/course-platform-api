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
    void frontendDeveUsarJwtSemCredenciaisBasic() throws IOException {
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
                    .doesNotContain("credentials: 'include'");
        }
    }

    @Test
    void clientesAutenticadosDevemEnviarBearerToken() throws IOException {
        for (String relative : List.of("aluno/aluno.js", "curador/curador.js", "professor/professor.js")) {
            String content = Files.readString(frontend.resolve(relative));
            assertThat(content)
                    .as("JWT Bearer de %s", relative)
                    .contains("Authorization: `Bearer ${session.token}`");
        }
    }
}
