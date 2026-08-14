package ananditos.sandraxandreia.repository;

import ananditos.sandraxandreia.domain.curador.Curador;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CuradorRepository extends JpaRepository<Curador, Long> {
}
