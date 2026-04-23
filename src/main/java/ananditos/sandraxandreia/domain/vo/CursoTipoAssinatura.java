package ananditos.sandraxandreia.domain.vo;

import ananditos.sandraxandreia.domain.PlanoAssinatura;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class CursoTipoAssinatura {
    @Column(name = "tipo_assinatura", nullable = false)
    private String valor;

    protected CursoTipoAssinatura() {

    }

    public CursoTipoAssinatura(String valor) {
        String normalizado = valor == null ? null : valor.trim();
        if (normalizado == null || normalizado.isBlank()) {
            throw new IllegalArgumentException("Título é obrigatório");
        }

        if (!normalizado.equals(PlanoAssinatura.COMUM.name()) &&
                !normalizado.equals(PlanoAssinatura.PREMIUM.name())) {

            throw new IllegalArgumentException("Esse tipo de assinatura não existe");
        }
    }
}
