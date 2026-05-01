package ananditos.sandraxandreia.dto.response;

import ananditos.sandraxandreia.domain.curso.CursoAssinatura;
import ananditos.sandraxandreia.domain.curso.StatusCurso;
import ananditos.sandraxandreia.domain.curso.TipoCurso;
public class CursoResponseDTO {

    public String nome;

    private CursoAssinatura tipoAssinatura;

    private TipoCurso tipoCurso;

    private Long professorId;

    private StatusCurso status;

    public CursoResponseDTO(String nome, CursoAssinatura tipoAssinatura, TipoCurso tipoCurso, StatusCurso status,  Long professorId) {
        this.nome = nome;
        this.tipoAssinatura = tipoAssinatura;
        this.tipoCurso = tipoCurso;
        this.professorId = professorId;
        this.status = status;
    }

    public CursoResponseDTO() {
    }

    public String getNome() {
        return nome;
    }

    public CursoAssinatura getTipoAssinatura() {
        return tipoAssinatura;
    }

    public TipoCurso getTipoCurso() {
        return tipoCurso;
    }

    public StatusCurso getStatus() {
        return status;
    }

    public Long getProfessorId() {
        return professorId;
    }
}
