package ananditos.sandraxandreia.dto.response;

import ananditos.sandraxandreia.domain.curso.CursoAssinatura;
import ananditos.sandraxandreia.domain.curso.TipoCurso;
public class CursoResponseDTO {

    public String nome;

    private CursoAssinatura tipoAssinatura;

    private TipoCurso tipoCurso;

    private Long professorId;

    public CursoResponseDTO(String nome, CursoAssinatura tipoAssinatura, TipoCurso tipoCurso,  Long professorId) {
        this.nome = nome;
        this.tipoAssinatura = tipoAssinatura;
        this.tipoCurso = tipoCurso;
        this.professorId = professorId;
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

    public Long getProfessorId() {
        return professorId;
    }
}
