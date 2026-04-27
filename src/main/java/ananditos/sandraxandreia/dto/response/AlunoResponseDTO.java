package ananditos.sandraxandreia.dto.response;

import ananditos.sandraxandreia.domain.GeneroUsuario;
import ananditos.sandraxandreia.domain.StatusAluno;

import java.time.LocalDate;

public class AlunoResponseDTO extends UsuarioResponseDTO {

    private String ra;
    private StatusAluno status;

    public AlunoResponseDTO() {
    }

    public AlunoResponseDTO(Long id, String nome, String email, String cpf, GeneroUsuario genero, LocalDate dataNascimento, String ra, StatusAluno status) {
        super(id, nome, email, cpf, genero, dataNascimento);
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
