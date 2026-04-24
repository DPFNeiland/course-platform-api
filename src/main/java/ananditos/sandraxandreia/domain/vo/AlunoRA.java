package ananditos.sandraxandreia.domain.vo;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class AlunoRA {


    @Column(name = "ra", nullable = false, unique = true, length = 6)
    private String valor;

    protected AlunoRA() {}

    public AlunoRA(String valor) {
        String normalizado = valor == null ? null : valor.trim().toUpperCase(); // ← normaliza pra maiúsculo
        if (normalizado == null || normalizado.isBlank()) {
            throw new IllegalArgumentException("RA eh obrigatorio");
        }
        if (!isValido(normalizado)) {
            throw new IllegalArgumentException("RA invalido, deve seguir o formato AB1234");
        }
        this.valor = normalizado;
    }

    public String getValor() {
        return valor;
    }


    private boolean isValido(String ra) {
        return ra.matches("[A-Z]{2}\\d{4}");
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AlunoRA alunoRA = (AlunoRA) o;
        return Objects.equals(valor, alunoRA.valor);
    }

    @Override
    public int hashCode() {

        return Objects.hashCode(valor);
    }


}
