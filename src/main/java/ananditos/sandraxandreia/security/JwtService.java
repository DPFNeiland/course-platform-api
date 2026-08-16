package ananditos.sandraxandreia.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey secret;
    private final long expirationSeconds;

    public JwtService(@Value("${security.jwt.secret}") String secret,
                      @Value("${security.jwt.expiration-seconds:3600}") long expirationSeconds) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("security.jwt.secret deve possuir pelo menos 32 caracteres");
        }
        if (expirationSeconds <= 0) {
            throw new IllegalStateException("security.jwt.expiration-seconds deve ser maior que zero");
        }
        this.secret = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = expirationSeconds;
    }

    public TokenEmitido emitir(String subject) {
        Instant emitidoEm = Instant.now();
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
        if (token == null || token.isBlank()) {
            throw new JwtInvalidoException("TOKEN_INVALID", "Token JWT ausente");
        }
        try {
            String subject = Jwts.parser()
                    .verifyWith(secret)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            if (subject == null || subject.isBlank()) {
                throw new JwtInvalidoException("TOKEN_INVALID", "Claim sub obrigatoria ausente");
            }
            return subject;
        } catch (ExpiredJwtException ex) {
            throw new JwtInvalidoException("TOKEN_EXPIRED", "Token JWT expirado");
        } catch (JwtException | IllegalArgumentException ex) {
            throw new JwtInvalidoException("TOKEN_INVALID", "Token JWT invalido");
        }
    }

    public record TokenEmitido(String token, Instant expiraEm) {
    }
}
