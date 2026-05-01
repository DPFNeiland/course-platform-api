package ananditos.sandraxandreia.domain.curso;

import ananditos.sandraxandreia.domain.aluno.Aluno;
import ananditos.sandraxandreia.domain.matricula.Matricula;
import ananditos.sandraxandreia.domain.professor.Professor;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "curso")
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private CursoAssinatura tipoAssinatura;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private TipoCurso tipoCurso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private StatusCurso status;

    @ManyToOne
    @JoinColumn(name="professor_id", nullable=false)
    private Professor professor;

    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Matricula> alunos = new ArrayList<>();

    public Curso(Long id, String nome, CursoAssinatura tipoAssinatura, TipoCurso tipoCurso) {
        this.id = id;
        this.nome = nome;
        this.tipoAssinatura = tipoAssinatura;
        this.tipoCurso = tipoCurso;
        this.status = StatusCurso.EM_AVALIACAO;

    }

    public Curso() {
    }

    public List<Matricula> getAlunos() {
        return alunos;
    }

    public void setAlunos(List<Matricula> alunos) {
        this.alunos = alunos;
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

    public StatusCurso getStatus() {
        return status;
    }

    public void setStatus(StatusCurso status) {
        this.status = status;
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
