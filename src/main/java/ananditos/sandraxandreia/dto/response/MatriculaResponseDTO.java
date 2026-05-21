package ananditos.sandraxandreia.dto.response;

import ananditos.sandraxandreia.domain.matricula.StatusMatricula;

import java.time.LocalDate;

public class MatriculaResponseDTO {
    private Long id;

    private LocalDate dataMatricula;

    private StatusMatricula status;

    private Long alunoId;

    private Long cursoId;

    public MatriculaResponseDTO(Long id, LocalDate dataMatricula, StatusMatricula status, Long alunoId, Long cursoId) {
        this.id = id;
        this.dataMatricula = dataMatricula;
        this.status = status;
        this.alunoId = alunoId;
        this.cursoId = cursoId;
    }

    public MatriculaResponseDTO() {
    }

    public Long getId() {
        return id;
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
