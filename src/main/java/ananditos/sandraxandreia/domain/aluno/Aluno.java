// código documentado por Betina Volpi com o intuito de revisar a matéria,
// além de explicar como funciona para possíveis leitores que possam utilizá-lo
package ananditos.sandraxandreia.domain.aluno;

import ananditos.sandraxandreia.domain.matricula.Matricula;
import ananditos.sandraxandreia.domain.usuario.GeneroUsuario;
import ananditos.sandraxandreia.domain.usuario.Usuario;
import ananditos.sandraxandreia.domain.aluno.vo.AlunoRA;
import ananditos.sandraxandreia.domain.usuario.UsuarioCargo;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// indica que essa classe representa uma tabela do banco de dados
@Entity
@Table(name="aluno")
public class Aluno extends Usuario {

    // "imbutir" valor do AlunoRA na tabela Aluno (não criar uma nova)
    @Embedded
    private AlunoRA ra;

    @Enumerated(EnumType.STRING)
    private StatusAluno status;

    // relacionamento com outras tabelas
    @OneToMany(mappedBy = "aluno", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Matricula> cursos = new ArrayList<>();

    public Aluno() {
        // O Hibernate/JPA exige que toda entidade tenha
        // um construtor vazio. Busca os dados no Banco:
        // 1° cria Aluno vazio
        // 2° preenche os campos um a um.
    }

    public Aluno(Long id, String nome, String email, String senhaCriptografada, String cpf, GeneroUsuario genero, String dataNascimento, String ra, StatusAluno status) {
        // super comunica com o Usuario (classe pai)
        super(id, nome, email, senhaCriptografada, cpf, genero, dataNascimento, UsuarioCargo.ALUNO);
        this.ra = new AlunoRA(ra);
        this.status = status;
    }

    public AlunoRA getRa() {
        return ra;
    }

    public void setRa(AlunoRA ra) {
        this.ra = ra;
    }

    public StatusAluno getStatus() {
        return status;
    }

    public void setStatus(StatusAluno status) {
        this.status = status;
    }

    public List<Matricula> getCursos() {
        return cursos;
    }

    public void setCursos(List<Matricula> cursos) {
        this.cursos = cursos;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Aluno aluno = (Aluno) o;
        return Objects.equals(ra, aluno.ra) && status == aluno.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), ra, status);
    }

    @Override
    public String toString() {
        return "Aluno{" +
                "RA=" + ra +
                ", Status=" + status +
                '}';
    }
}
