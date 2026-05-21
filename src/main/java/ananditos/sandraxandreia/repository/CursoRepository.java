package ananditos.sandraxandreia.repository;

import ananditos.sandraxandreia.domain.curso.Curso;
import ananditos.sandraxandreia.domain.curso.StatusCurso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CursoRepository extends JpaRepository<Curso, Long> {
    List<Curso> findByStatus(StatusCurso status);
    List<Curso> findByStatusOrderByNomeAsc(StatusCurso status);
    List<Curso> findByProfessorId(Long professorId);
    boolean existsByNome(String nome);
    boolean existsByNomeIgnoreCase(String nome);
}
