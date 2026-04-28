package ananditos.sandraxandreia.dto.request;

import ananditos.sandraxandreia.domain.matricula.StatusMatricula;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MatriculaRequestDTO {

    @NotNull(message = "Status da matrihcula invahlida")
    private StatusMatricula status;

    @NotNull(message = "Aluno Obrigatohrio")
    private Long alunoId;

    @NotNull(message = "curso_id")
    private Long cursoId;

    public MatriculaRequestDTO() {
    }


    public StatusMatricula getStatus() {
        return status;
    }

    public Long getAlunoId() {
        return alunoId;
    }

    public Long getCursoId() {
        return cursoId;
    }
}
