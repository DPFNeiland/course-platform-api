package ananditos.sandraxandreia.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTests {

    private static final String SECRET = "test-secret-with-more-than-thirty-two-characters";

    @Test
    void deveRejeitarTokenExpirado() throws InterruptedException {
        JwtService service = new JwtService(SECRET, 1);
        String token = service.emitir("aluno@teste.com").token();

        Thread.sleep(1100);

        assertThatThrownBy(() -> service.validarEObterSubject(token))
                .isInstanceOf(JwtInvalidoException.class)
                .hasMessageContaining("expirado")
                .extracting("codigo")
                .isEqualTo("TOKEN_EXPIRED");
    }

    @Test
    void deveRejeitarTokenMalformado() {
        JwtService service = new JwtService(SECRET, 3600);

        assertThatThrownBy(() -> service.validarEObterSubject("nao-e-um-jwt"))
                .isInstanceOf(JwtInvalidoException.class)
                .extracting("codigo")
                .isEqualTo("TOKEN_INVALID");
    }

    @Test
    void deveFalharAoInicializarComConfiguracaoInsegura() {
        assertThatThrownBy(() -> new JwtService("segredo-curto", 3600))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 caracteres");
        assertThatThrownBy(() -> new JwtService(SECRET, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("maior que zero");
    }
}
