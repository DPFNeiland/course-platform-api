package ananditos.sandraxandreia.repository;

import ananditos.sandraxandreia.domain.curso.Curso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CursoRepository extends JpaRepository<Curso, Long> {
}
