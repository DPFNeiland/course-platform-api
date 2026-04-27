package ananditos.sandraxandreia.domain.usuario.vo;

import jakarta.persistence.Column;

import java.util.Objects;
import java.util.regex.Pattern;

public class UsuarioEmail {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Column(name = "email", nullable = false, unique = true)
    private String valor;

    protected UsuarioEmail() {
    }

    public UsuarioEmail(String valor) {
        String normalizado = valor == null ? null : valor.trim().toLowerCase();

        if (normalizado == null || normalizado.isBlank()) {
            throw new IllegalArgumentException("E-mail e obrigatorio");
        }
        if (!EMAIL_PATTERN.matcher(normalizado).matches()) {
            throw new IllegalArgumentException("E-mail invalido");
        }

        this.valor = normalizado;
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UsuarioEmail that)) return false;
        return Objects.equals(valor, that.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }
}
