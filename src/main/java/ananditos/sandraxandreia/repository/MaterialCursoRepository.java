package ananditos.sandraxandreia.repository;

import ananditos.sandraxandreia.domain.curso.MaterialCurso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaterialCursoRepository extends JpaRepository<MaterialCurso, Long> {
    List<MaterialCurso> findByCursoIdOrderByDataCadastroAsc(Long cursoId);
    Optional<MaterialCurso> findByIdAndCursoId(Long id, Long cursoId);
}
