package ananditos.sandraxandreia.repository;

import ananditos.sandraxandreia.domain.Professor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorRepository extends JpaRepository<Professor, Long> {
}
