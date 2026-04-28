package ananditos.sandraxandreia.domain.professor;

import ananditos.sandraxandreia.domain.curso.Curso;
import ananditos.sandraxandreia.domain.usuario.GeneroUsuario;
import ananditos.sandraxandreia.domain.usuario.Usuario;
import ananditos.sandraxandreia.domain.professor.vo.ProfessorAreaFormacao;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "professor")
public class Professor extends Usuario {

    @Column(nullable = false, length = 100)
    private ProfessorAreaFormacao areaFormacao;

    @Column(nullable = false)
    private double horaAula;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEnsinoProfessor tipoEnsino;

    @OneToMany(
            mappedBy = "professor",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Curso> cursos = new ArrayList<>();

    public Professor() {
    }

    public Professor(Long id, String nome, String email, String senhaCriptografada, String cpf, GeneroUsuario genero, String dataNascimento, String areaFormacao, double horaAula, TipoEnsinoProfessor tipoEnsino) {
        super(id, nome, email, senhaCriptografada, cpf, genero, dataNascimento);
        this.areaFormacao = new ProfessorAreaFormacao(areaFormacao);
        this.horaAula = horaAula;
        this.tipoEnsino = tipoEnsino;
    }
    public ProfessorAreaFormacao getAreaFormacao() {
        return areaFormacao;
    }

    public void setAreaFormacao(ProfessorAreaFormacao areaFormacao) {
        this.areaFormacao = areaFormacao;
    }

    public double getHoraAula() {
        return horaAula;
    }

    public void setHoraAula(double horaAula) {
        this.horaAula = horaAula;
    }

    public TipoEnsinoProfessor getTipoEnsino() {
        return tipoEnsino;
    }

    public void setTipoEnsino(TipoEnsinoProfessor tipoEnsino) {
        this.tipoEnsino = tipoEnsino;
    }

    public List<Curso> getCursos() {
        return cursos;
    }

    public void setCursos(List<Curso> cursos) {
        this.cursos = cursos;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Professor professor = (Professor) o;
        return Double.compare(horaAula, professor.horaAula) == 0 && Objects.equals(areaFormacao, professor.areaFormacao) && tipoEnsino == professor.tipoEnsino;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), areaFormacao, horaAula, tipoEnsino);
    }

    @Override
    public String toString() {
        return "Professor{" +
                "areaFormacao=" + areaFormacao +
                ", horaAula=" + horaAula +
                ", tipoEnsino=" + tipoEnsino +
                '}';
    }
}

