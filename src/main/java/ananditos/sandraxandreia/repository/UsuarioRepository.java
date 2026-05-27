package ananditos.sandraxandreia.repository;

import ananditos.sandraxandreia.domain.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmailValor(String email);
    boolean existsByEmailValor(String email);
    boolean existsByCpfValor(String email);
    Optional<Usuario> findByEmailValor(String email);
}
