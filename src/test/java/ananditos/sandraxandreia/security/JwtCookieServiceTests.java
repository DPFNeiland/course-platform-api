package ananditos.sandraxandreia.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

import static org.assertj.core.api.Assertions.assertThat;

class JwtCookieServiceTests {

    @Test
    void cookieDeSessaoDeveSerSeguroPorPadraoDeProducao() {
        ResponseCookie cookie = new JwtCookieService(true, 3600).criar("jwt");

        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.isSecure()).isTrue();
        assertThat(cookie.getSameSite()).isEqualTo("Strict");
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getMaxAge().getSeconds()).isEqualTo(3600);
    }

    @Test
    void cookiePodeDesabilitarSecureExplicitamenteApenasParaHttpLocal() {
        ResponseCookie cookie = new JwtCookieService(false, 3600).criar("jwt");

        assertThat(cookie.isSecure()).isFalse();
        assertThat(cookie.isHttpOnly()).isTrue();
    }
}
