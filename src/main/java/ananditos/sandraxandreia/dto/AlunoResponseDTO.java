package ananditos.sandraxandreia.dto;

import ananditos.sandraxandreia.domain.GeneroUsuario;
import ananditos.sandraxandreia.domain.StatusAluno;
import ananditos.sandraxandreia.domain.vo.AlunoRA;

public class AlunoResponseDTO extends UsuarioResponseDTO {

    private String ra;
    private StatusAluno status;

    public AlunoResponseDTO() {
    }

    public AlunoResponseDTO(Long id, String nome, String email, String cpf, GeneroUsuario genero, String ra, StatusAluno status) {
        super(id, nome, email, cpf, genero);
        this.ra = ra;
        this.status = status;
    }


    public String getRa() {
        return ra;
    }

    public StatusAluno getStatus() {
        return status;
    }
}
