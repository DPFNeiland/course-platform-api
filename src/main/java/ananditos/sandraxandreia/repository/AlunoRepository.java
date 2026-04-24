package ananditos.sandraxandreia.repository;

import ananditos.sandraxandreia.domain.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {
    boolean existsByEmailValor(String email);
    boolean existsByCpfValor(String email);
}
