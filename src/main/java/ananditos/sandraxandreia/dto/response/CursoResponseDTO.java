package ananditos.sandraxandreia.dto.response;

import ananditos.sandraxandreia.domain.curso.CursoAssinatura;
import ananditos.sandraxandreia.domain.curso.TipoCurso;
public class CursoResponseDTO {

    public String nome;

    private CursoAssinatura tipoAssinatura;

    private TipoCurso tipoCurso;

    public CursoResponseDTO(String nome, CursoAssinatura tipoAssinatura, TipoCurso tipoCurso) {
        this.nome = nome;
        this.tipoAssinatura = tipoAssinatura;
        this.tipoCurso = tipoCurso;
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
}
