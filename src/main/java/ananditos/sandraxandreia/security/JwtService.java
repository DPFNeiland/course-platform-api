package ananditos.sandraxandreia.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class JwtService {
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();
    private static final String HEADER = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private static final Pattern SUBJECT_PATTERN = Pattern.compile("\"sub\":\"((?:\\\\.|[^\"\\\\])*)\"");
    private static final Pattern EXPIRATION_PATTERN = Pattern.compile("\"exp\":(\\d+)");

    private final byte[] secret;
    private final long expirationSeconds;

    public JwtService(@Value("${security.jwt.secret}") String secret,
                      @Value("${security.jwt.expiration-seconds:3600}") long expirationSeconds) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("security.jwt.secret deve possuir pelo menos 32 caracteres");
        }
        if (expirationSeconds <= 0) {
            throw new IllegalStateException("security.jwt.expiration-seconds deve ser maior que zero");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationSeconds;
    }

    public TokenEmitido emitir(String subject) {
        Instant emitidoEm = Instant.now();
        Instant expiraEm = emitidoEm.plusSeconds(expirationSeconds);
        String header = encode(HEADER);
        String payload = encode("{\"sub\":\"" + escape(subject) + "\",\"iat\":"
                + emitidoEm.getEpochSecond() + ",\"exp\":" + expiraEm.getEpochSecond() + "}");
        String conteudoAssinado = header + "." + payload;
        String token = conteudoAssinado + "." + BASE64_URL_ENCODER.encodeToString(assinar(conteudoAssinado));
        return new TokenEmitido(token, expiraEm);
    }

    public String validarEObterSubject(String token) {
        if (token == null) {
            throw new JwtInvalidoException("Token JWT ausente");
        }
        String[] partes = token.split("\\.", -1);
        if (partes.length != 3) {
            throw new JwtInvalidoException("Token JWT malformado");
        }

        String conteudoAssinado = partes[0] + "." + partes[1];
        byte[] assinaturaRecebida;
        String payload;
        try {
            assinaturaRecebida = BASE64_URL_DECODER.decode(partes[2]);
            payload = new String(BASE64_URL_DECODER.decode(partes[1]), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            throw new JwtInvalidoException("Token JWT malformado");
        }

        if (!MessageDigest.isEqual(assinar(conteudoAssinado), assinaturaRecebida)) {
            throw new JwtInvalidoException("Assinatura JWT invalida");
        }

        Matcher expiration = EXPIRATION_PATTERN.matcher(payload);
        Matcher subject = SUBJECT_PATTERN.matcher(payload);
        if (!expiration.find() || !subject.find()) {
            throw new JwtInvalidoException("Claims JWT obrigatorias ausentes");
        }
        if (Instant.now().getEpochSecond() >= Long.parseLong(expiration.group(1))) {
            throw new JwtInvalidoException("Token JWT expirado");
        }
        return unescape(subject.group(1));
    }

    private byte[] assinar(String conteudo) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(conteudo.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("Nao foi possivel assinar o JWT", ex);
        }
    }

    private String encode(String value) {
        return BASE64_URL_ENCODER.encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String unescape(String value) {
        return value.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    public record TokenEmitido(String token, Instant expiraEm) {
    }
}
