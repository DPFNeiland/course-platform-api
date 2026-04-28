package ananditos.sandraxandreia.domain.matricula;

import ananditos.sandraxandreia.domain.aluno.Aluno;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "matricula")
public class Matricula {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String dataMatricula;

    @Column(nullable = false, length = 100)
    private StatusMatricula status;

    public void manterMatricula(){
        System.out.println( "Matricula mantida");
    };

    public void matricular(Aluno aluno) {
        System.out.println(" Pessoa matriculada");
    };

    public Matricula(Long id, StatusMatricula status, String dataMatricula) {
        this.id = id;
        this.status = status;
        this.dataMatricula = dataMatricula;
    }

    public Matricula() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDataMatricula() {
        return dataMatricula;
    }

    public void setDataMatricula(String dataMatricula) {
        this.dataMatricula = dataMatricula;
    }

    public StatusMatricula getStatus() {
        return status;
    }

    public void setStatus(StatusMatricula status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Matricula matricula = (Matricula) o;
        return Objects.equals(id, matricula.id) && Objects.equals(dataMatricula, matricula.dataMatricula) && status == matricula.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dataMatricula, status);
    }

    @Override
    public String toString() {
        return "Matricula{" +
                "id=" + id +
                ", dataMatricula='" + dataMatricula + '\'' +
                ", status=" + status +
                '}';
    }
}
