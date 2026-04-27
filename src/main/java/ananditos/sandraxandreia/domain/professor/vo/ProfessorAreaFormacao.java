package ananditos.sandraxandreia.domain.professor.vo;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class ProfessorAreaFormacao {


    @Column(name = "areaFormacao", nullable = false, length = 100)
    private String valor;

    protected ProfessorAreaFormacao() {}

    public ProfessorAreaFormacao(String valor) {
        String normalizado = valor == null ? null : valor.trim().toLowerCase();

        if (normalizado == null || normalizado.isBlank()) {
            throw new IllegalArgumentException("A área de formação é obrigatória");
        }
        this.valor = normalizado;
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProfessorAreaFormacao that)) return false;
        return Objects.equals(valor, that.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }

}