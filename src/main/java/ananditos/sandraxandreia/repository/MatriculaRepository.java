package ananditos.sandraxandreia.repository;

import ananditos.sandraxandreia.domain.matricula.Matricula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {
    boolean existsByAluno_IdAndCurso_Id(Long alunoId, Long cursoId);
    List<Matricula> findByAluno_Id(Long alunoId);
    List<Matricula> findByCurso_Id(Long cursoId);
}
