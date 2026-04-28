package ananditos.sandraxandreia.domain.matricula;

import ananditos.sandraxandreia.domain.aluno.Aluno;
import ananditos.sandraxandreia.domain.curso.Curso;
import ananditos.sandraxandreia.domain.matricula.vo.MatriculaDataMatricula;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "matricula")
public class Matricula {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private MatriculaDataMatricula dataMatricula;

    @Column(nullable = false, length = 100)
    private StatusMatricula status;

    @ManyToOne
    @JoinColumn(name = "aluno_id")
    private Aluno aluno;

    @ManyToOne
    @JoinColumn(name = "curso_id")
    private Curso curso;

    public void manterMatricula(){
        System.out.println( "Matricula mantida");
    };

    public void matricular(Aluno aluno) {
        System.out.println(" Pessoa matriculada");
    };

    public Matricula(Long id, StatusMatricula status) {
        this.id = id;
        this.status = status;
        this.dataMatricula = MatriculaDataMatricula.agora();;
    }

    public Matricula() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MatriculaDataMatricula getDataMatricula() {
        return dataMatricula;
    }

    public void setDataMatricula(MatriculaDataMatricula dataMatricula) {
        this.dataMatricula = dataMatricula;
    }

    public StatusMatricula getStatus() {
        return status;
    }

    public void setStatus(StatusMatricula status) {
        this.status = status;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public void setAluno(Aluno aluno) {
        this.aluno = aluno;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Matricula matricula = (Matricula) o;
        return Objects.equals(id, matricula.id) && Objects.equals(dataMatricula, matricula.dataMatricula) && status == matricula.status && Objects.equals(aluno, matricula.aluno) && Objects.equals(curso, matricula.curso);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dataMatricula, status, aluno, curso);
    }

    @Override
    public String toString() {
        return "Matricula{" +
                "id=" + id +
                ", dataMatricula=" + dataMatricula +
                ", status=" + status +
                ", aluno=" + aluno +
                ", curso=" + curso +
                '}';
    }
}
