package ananditos.sandraxandreia.repository;

import ananditos.sandraxandreia.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
