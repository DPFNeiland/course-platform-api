package ananditos.sandraxandreia.repository;

import ananditos.sandraxandreia.domain.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    boolean existsByEmailValor(String email);
    boolean existsByCpfValor(String email);
}
