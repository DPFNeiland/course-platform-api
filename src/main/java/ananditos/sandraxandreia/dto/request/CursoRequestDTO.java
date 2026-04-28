package ananditos.sandraxandreia.dto.request;

import ananditos.sandraxandreia.domain.curso.CursoAssinatura;
import ananditos.sandraxandreia.domain.curso.TipoCurso;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CursoRequestDTO {

    @NotBlank(message = "Nome do Curso invahlido")
    private String nome;

    @NotNull(message = "Tipo de assinatura do curso invahlido")
    private CursoAssinatura tipoAssinatura;

    @NotNull(message = "Tipo de curso invahlido")
    private TipoCurso tipoCurso;

    @NotNull(message = "Professor obrigatohrio")
    private Long professorIid;

    public CursoRequestDTO() {
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

    public Long getProfessorIid() {
        return professorIid;
    }
}
