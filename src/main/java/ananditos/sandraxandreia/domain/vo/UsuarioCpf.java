package ananditos.sandraxandreia.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class UsuarioCpf {

    @Column(name = "cpf", nullable = false)
    private String valor;

    protected UsuarioCpf() {

    }

    public UsuarioCpf(String valor) {
        String normalizado = valor == null ? null : valor.trim();
        if (normalizado == null || normalizado.isBlank()) {
            throw new IllegalArgumentException("CPF eh obrigatoria");
        }
        this.valor = normalizado;
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UsuarioCpf that = (UsuarioCpf) o;
        return Objects.equals(valor, that.valor);
    }
}
