package ananditos.sandraxandreia.domain.usuario.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class UsuarioSenhaCriptografada {

    @Column(name = "senha", nullable = false)
    private String valor;

    protected UsuarioSenhaCriptografada() {

    }

    public UsuarioSenhaCriptografada(String valor) {
        String normalizado = valor == null ? null : valor.trim();
        if (normalizado == null || normalizado.isBlank()) {
            throw new IllegalArgumentException("Senha e obrigatoria");
        }
        this.valor = normalizado;
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UsuarioSenhaCriptografada that = (UsuarioSenhaCriptografada) o;
        return Objects.equals(valor, that.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(valor);
    }
}
