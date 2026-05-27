package ananditos.sandraxandreia.domain.curador;

import ananditos.sandraxandreia.domain.usuario.GeneroUsuario;
import ananditos.sandraxandreia.domain.usuario.Usuario;
import ananditos.sandraxandreia.domain.usuario.UsuarioCargo;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "curador")
public class Curador extends Usuario {

    public Curador() {
    }

    public Curador(Long id, String nome, String email, String cpf, String senha, String dataNascimento, GeneroUsuario genero) {
        super(id, nome, email, cpf, senha, dataNascimento, genero, UsuarioCargo.CURADOR);
    }
}
