package ananditos.sandraxandreia.dto.response;

import ananditos.sandraxandreia.domain.usuario.GeneroUsuario;
import ananditos.sandraxandreia.domain.usuario.UsuarioCargo;

import java.time.LocalDate;

public class CuradorResponseDTO extends UsuarioResponseDTO {

    public CuradorResponseDTO() {
    }

    public CuradorResponseDTO(Long id, String nome, String email, String cpf, GeneroUsuario genero, LocalDate dataNascimento, UsuarioCargo cargo) {
        super(id, nome, email, cpf, genero, dataNascimento, cargo);
    }
}
