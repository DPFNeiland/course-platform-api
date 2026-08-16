package ananditos.sandraxandreia.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTests {

    @Test
    void deveRejeitarTokenExpirado() throws InterruptedException {
        JwtService service = new JwtService("test-secret-with-more-than-thirty-two-characters", 1);
        String token = service.emitir("aluno@teste.com").token();

        Thread.sleep(1100);

        assertThatThrownBy(() -> service.validarEObterSubject(token))
                .isInstanceOf(JwtInvalidoException.class)
                .hasMessageContaining("expirado")
                .extracting("codigo")
                .isEqualTo("TOKEN_EXPIRED");
    }
}
