package ananditos.sandraxandreia.domain;

import ananditos.sandraxandreia.domain.vo.UsuarioEmail;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "professor")
public class Professor extends Usuario{

    @Column(nullable = false, length = 100)
    private String areaFormacao;

    @Column(nullable = false)
    private double horaAula;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEnsinoProfessor tipoEnsino;

    public Professor() {
    }

    public Professor(Long id, String nome, String email, String senhaCriptografada, String cpf, String areaFormacao, GeneroUsuario genero, double horaAula, TipoEnsinoProfessor tipoEnsino) {
        super(id, nome, email, senhaCriptografada, cpf, genero);
        this.areaFormacao = areaFormacao;
        this.horaAula = horaAula;
        this.tipoEnsino = tipoEnsino;
    }

    public void setAreaFormacao(String areaFormacao) {
        this.areaFormacao = areaFormacao;
    }

    public void setHoraAula(double horaAula) {
        this.horaAula = horaAula;
    }

    public void setTipoEnsino(TipoEnsinoProfessor tipoEnsino) {
        this.tipoEnsino = tipoEnsino;
    }

    public String getAreaFormacao() {
        return areaFormacao;
    }

    public double getHoraAula() {
        return horaAula;
    }

    public TipoEnsinoProfessor getTipoEnsino() {
        return tipoEnsino;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Professor professor = (Professor) o;
        return Double.compare(horaAula, professor.horaAula) == 0 && Objects.equals(areaFormacao, professor.areaFormacao) && Objects.equals(tipoEnsino, professor.tipoEnsino);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), areaFormacao, horaAula, tipoEnsino);
    }

    @Override
    public String toString() {
        return "Professor{" +
                "areaFormacao='" + areaFormacao + '\'' +
                ", horaAula=" + horaAula +
                ", tipoEnsino='" + tipoEnsino + '\'' +
                '}';
    }
}

