package ananditos.sandraxandreia.dto;

import ananditos.sandraxandreia.domain.TipoEnsinoProfessor;
import jakarta.validation.constraints.NotNull;

public class ProfessorRequestDTO extends UsuarioRequestDTO {

    @NotNull(message = "Area de formacao em branco")
    private String areaFormacao;

    @NotNull(message = "Hora aula e obrigatoria")
    private double horaAula;

    @NotNull(message = "Tipo de ensino e obrigatorio")
    private TipoEnsinoProfessor tipoEnsino;

    public ProfessorRequestDTO() {
    }

    public String getAreaFormacao() {
        return areaFormacao;
    }

    public double getHoraAula() {
        return horaAula;
    }

    public TipoEnsinoProfessor getTipoEnsino() {
        return tipoEnsino;
    }
}
