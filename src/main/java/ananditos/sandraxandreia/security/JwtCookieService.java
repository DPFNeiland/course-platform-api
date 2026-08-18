package ananditos.sandraxandreia.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@Service
public class JwtCookieService {
    public static final String COOKIE_NAME = "SXA_SESSION";

    private final boolean secure;
    private final Duration expiration;

    public JwtCookieService(@Value("${security.jwt.cookie-secure:true}") boolean secure,
                            @Value("${security.jwt.expiration-seconds:3600}") long expirationSeconds) {
        this.secure = secure;
        this.expiration = Duration.ofSeconds(expirationSeconds);
    }

    public ResponseCookie criar(String token) {
        return cookie(token, expiration);
    }

    public ResponseCookie remover() {
        return cookie("", Duration.ZERO);
    }

    public Optional<String> obterToken(HttpServletRequest request) {
        if (request.getCookies() == null) return Optional.empty();
        return Arrays.stream(request.getCookies())
                .filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(value -> !value.isBlank())
                .findFirst();
    }

    private ResponseCookie cookie(String value, Duration maxAge) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path("/")
                .maxAge(maxAge)
                .build();
    }
}
