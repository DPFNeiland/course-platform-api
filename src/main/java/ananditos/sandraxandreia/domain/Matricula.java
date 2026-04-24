package ananditos.sandraxandreia.domain;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "matricula")
public class Matricula {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private StatusMatricula status;

    public void manterMatricula(){
        System.out.println( "Matricula mantida");
    };

    public void matricular(Aluno aluno) {
        System.out.println(" Pessoa matriculada");
    };

    public Matricula(Long id, StatusMatricula status) {
        this.id = id;
        this.status = status;
    }

    public Matricula() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        return Objects.equals(id, matricula.id) && status == matricula.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, status);
    }

    @Override
    public String toString() {
        return "Matricula{" +
                "id=" + id +
                ", status=" + status +
                '}';
    }
}
