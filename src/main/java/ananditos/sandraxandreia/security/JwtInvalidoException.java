package ananditos.sandraxandreia.security;

public class JwtInvalidoException extends RuntimeException {
    private final String codigo;

    public JwtInvalidoException(String codigo, String message) {
        super(message);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
