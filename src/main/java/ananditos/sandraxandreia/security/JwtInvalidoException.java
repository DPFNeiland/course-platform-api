package ananditos.sandraxandreia.security;

public class JwtInvalidoException extends RuntimeException {
    public JwtInvalidoException(String message) {
        super(message);
    }
}
