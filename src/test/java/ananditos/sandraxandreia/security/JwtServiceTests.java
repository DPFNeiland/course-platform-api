package ananditos.sandraxandreia.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTests {

    private static final String SECRET = "test-secret-with-more-than-thirty-two-characters";

    @Test
    void deveRejeitarTokenExpirado() {
        Instant issuedAt = Instant.parse("2026-01-01T00:00:00Z");
        JwtService issuer = new JwtService(SECRET, 1, Clock.fixed(issuedAt, ZoneOffset.UTC));
        String token = issuer.emitir("aluno@teste.com").token();
        JwtService validator = new JwtService(SECRET, 1,
                Clock.fixed(issuedAt.plusSeconds(2), ZoneOffset.UTC));

        assertThatThrownBy(() -> validator.validarEObterSubject(token))
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
