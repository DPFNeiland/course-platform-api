// código documentado por Betina Volpi com o intuito de revisar a matéria,
// além de explicar como funciona para possíveis leitores que possam utilizá-lo
package ananditos.sandraxandreia.repository;

import ananditos.sandraxandreia.domain.aluno.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;

// Ao estender a interface JpaRepository, este repositório herda automaticamente
// métodos padronizados para operações de CRUD e eliminando a necessidade de
// escrever comandos SQL manualmente
public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    // procura o dentro do Aluno, ele é uma classe então procura o atributo valor
    // como se fosse: aluno.getEmail().getValor()
    boolean existsByEmailValor(String email);
    boolean existsByCpfValor(String cpf);
}
