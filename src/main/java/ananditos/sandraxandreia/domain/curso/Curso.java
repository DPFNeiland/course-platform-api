package ananditos.sandraxandreia.domain.curso;

import ananditos.sandraxandreia.domain.professor.Professor;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "curso")
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    @Column(nullable = false, length = 100)
    private CursoAssinatura tipoAssinatura;

    @Column(nullable = false, length = 100)
    private TipoCurso tipoCurso;

    @ManyToOne
    @JoinColumn(name="professor_id", nullable=false)
    private Professor professor;

    public Curso(Long id, String nome, CursoAssinatura tipoAssinatura, TipoCurso tipoCurso) {
        this.id = id;
        this.nome = nome;
        this.tipoAssinatura = tipoAssinatura;
        this.tipoCurso = tipoCurso;

    }

    public Curso() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public CursoAssinatura getTipoAssinatura() {
        return tipoAssinatura;
    }

    public void setTipoAssinatura(CursoAssinatura tipoAssinatura) {
        this.tipoAssinatura = tipoAssinatura;
    }

    public TipoCurso getTipoCurso() {
        return tipoCurso;
    }

    public void setTipoCurso(TipoCurso tipoCurso) {
        this.tipoCurso = tipoCurso;
    }

    public Professor getProfessor() {
        return professor;
    }

    public void setProfessor(Professor professor) {
        this.professor = professor;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Curso curso = (Curso) o;
        return Objects.equals(id, curso.id) && Objects.equals(nome, curso.nome) && tipoAssinatura == curso.tipoAssinatura && tipoCurso == curso.tipoCurso;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome, tipoAssinatura, tipoCurso);
    }

    @Override
    public String toString() {
        return "Curso{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", tipoAssinatura=" + tipoAssinatura +
                ", tipoCurso=" + tipoCurso +
                '}';
    }
}
