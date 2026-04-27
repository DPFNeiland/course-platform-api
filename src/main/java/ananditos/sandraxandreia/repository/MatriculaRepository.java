package ananditos.sandraxandreia.repository;

import ananditos.sandraxandreia.domain.matricula.Matricula;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {
}
