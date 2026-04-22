package ananditos.sandraxandreia.domain;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "professor")
public class Professor extends Usuario{

    @Column(nullable = false, length = 100)
    private String areaFormacao;

    @Column(nullable = false, length = 100)
    private double horaAula;

    @Column(nullable = false, length = 100)
    private String tipoEnsino;


    public Professor() {
    }


    public Professor(String nome, String email, String areaFormacao, double horaAula, String tipoEnsino) {
        super(nome, email);
        this.areaFormacao = areaFormacao;
        this.horaAula = horaAula;
        this.tipoEnsino = tipoEnsino;
    }


    public String getAreaFormacao() {
        return areaFormacao;
    }

    public void setAreaFormacao(String areaFormacao) {
        areaFormacao = areaFormacao;
    }

    public double getHoraAula() {
        return horaAula;
    }

    public void setHoraAula(double horaAula) {
        horaAula = horaAula;
    }

    public String getTipoEnsino() {
        return tipoEnsino;
    }

    public void setTipoEnsino(String tipoEnsino) {
        tipoEnsino = tipoEnsino;
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
                "AreaFormacao='" + areaFormacao + '\'' +
                ", HoraAula=" + horaAula +
                ", TipoEnsino='" + tipoEnsino + '\'' +
                '}';
    }
}

