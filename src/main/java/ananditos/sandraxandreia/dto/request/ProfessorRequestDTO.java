package ananditos.sandraxandreia.dto.request;

import ananditos.sandraxandreia.domain.professor.TipoEnsinoProfessor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ProfessorRequestDTO extends UsuarioRequestDTO {

    @NotBlank(message = "Area de formacao em branco")
    private String areaFormacao;

    @Positive(message = "Hora aula deve ser maior que zero")
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
