package ananditos.sandraxandreia.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey secret;
    private final long expirationSeconds;
    private final Clock clock;

    @Autowired
    public JwtService(@Value("${security.jwt.secret}") String secret,
                      @Value("${security.jwt.expiration-seconds:3600}") long expirationSeconds) {
        this(secret, expirationSeconds, Clock.systemUTC());
    }

    public JwtService(String secret, long expirationSeconds, Clock clock) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("security.jwt.secret deve possuir pelo menos 32 caracteres");
        }
        if (expirationSeconds <= 0) {
            throw new IllegalStateException("security.jwt.expiration-seconds deve ser maior que zero");
        }
        this.secret = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = expirationSeconds;
        this.clock = clock;
    }

    public TokenEmitido emitir(String subject) {
        Instant emitidoEm = clock.instant();
        Instant expiraEm = emitidoEm.plusSeconds(expirationSeconds);
        String token = Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(emitidoEm))
                .expiration(Date.from(expiraEm))
                .signWith(secret)
                .compact();
        return new TokenEmitido(token, expiraEm);
    }

    public String validarEObterSubject(String token) {
        return validar(token).subject();
    }

    public TokenValidado validar(String token) {
        if (token == null || token.isBlank()) {
            throw new JwtInvalidoException("TOKEN_INVALID", "Token JWT ausente");
        }
        try {
            var claims = Jwts.parser()
                    .verifyWith(secret)
                    .clock(() -> Date.from(clock.instant()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String subject = claims.getSubject();
            if (subject == null || subject.isBlank()) {
                throw new JwtInvalidoException("TOKEN_INVALID", "Claim sub obrigatoria ausente");
            }
            return new TokenValidado(subject, claims.getExpiration().toInstant());
        } catch (ExpiredJwtException ex) {
            throw new JwtInvalidoException("TOKEN_EXPIRED", "Token JWT expirado");
        } catch (JwtException | IllegalArgumentException ex) {
            throw new JwtInvalidoException("TOKEN_INVALID", "Token JWT invalido");
        }
    }

    public record TokenEmitido(String token, Instant expiraEm) {
    }

    public record TokenValidado(String subject, Instant expiraEm) {
    }
}
