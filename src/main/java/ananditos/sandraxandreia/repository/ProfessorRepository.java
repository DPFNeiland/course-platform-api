package ananditos.sandraxandreia.repository;

import ananditos.sandraxandreia.domain.professor.Professor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorRepository extends JpaRepository<Professor, Long> {
    boolean existsByEmailValor(String email);
    boolean existsByCpfValor(String email);
}
