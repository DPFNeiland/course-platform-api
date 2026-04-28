package ananditos.sandraxandreia.dto.response;

import ananditos.sandraxandreia.domain.matricula.StatusMatricula;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;

public class MatriculaResponseDTO {
    private LocalDate dataMatricula;

    private StatusMatricula status;

    private Long alunoId;

    private Long cursoId;

    public MatriculaResponseDTO(LocalDate dataMatricula, StatusMatricula status, Long alunoId, Long cursoId) {
        this.dataMatricula = dataMatricula;
        this.status = status;
        this.alunoId = alunoId;
        this.cursoId = cursoId;
    }

    public MatriculaResponseDTO() {
    }

    public LocalDate getDataMatricula() {
        return dataMatricula;
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
