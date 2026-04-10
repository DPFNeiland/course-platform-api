package ananditos.sandraxandreia.domain;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "professore")
public class Professor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String AreaFormacao;

    @Column(nullable = false, length = 100)
    private double HoraAula;

    @Column(nullable = false, length = 100)
    private String TipoEnsino;

    public Professor() {
        // Construtor padrao exigido pela JPA.
    }

    public Professor(String AreaFormacao, double HoraAula, String TipoEnsino) {
        this.AreaFormacao = AreaFormacao;
        this.HoraAula = HoraAula;
        this.TipoEnsino = TipoEnsino;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAreaFormacao() {
        return AreaFormacao;
    }

    public void setAreaFormacao(String areaFormacao) {
        AreaFormacao = areaFormacao;
    }

    public double getHoraAula() {
        return HoraAula;
    }

    public void setHoraAula(double horaAula) {
        HoraAula = horaAula;
    }

    public String getTipoEnsino() {
        return TipoEnsino;
    }

    public void setTipoEnsino(String tipoEnsino) {
        TipoEnsino = tipoEnsino;
    }

    @Override
    public String toString() {
        return "Professor{" +
                "id=" + id +
                ", AreaFormacao='" + AreaFormacao + '\'' +
                ", HoraAula=" + HoraAula +
                ", TipoEnsino='" + TipoEnsino + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Professor professor = (Professor) o;
        return Double.compare(HoraAula, professor.HoraAula) == 0 && Objects.equals(id, professor.id) && Objects.equals(AreaFormacao, professor.AreaFormacao) && Objects.equals(TipoEnsino, professor.TipoEnsino);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, AreaFormacao, HoraAula, TipoEnsino);
    }
}

