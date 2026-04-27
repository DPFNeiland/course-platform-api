package ananditos.sandraxandreia.domain.curso;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "curso")
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 100)
    private String tipoAssinatura;

    @Column(nullable = false, length = 100)
    private TipoCurso tipoCurso;


    public Curso(Long id, String nome, String tipoAssinatura, TipoCurso tipoCurso) {
        this.id = id;
        this.nome = nome;
        this.tipoAssinatura = tipoAssinatura;
        this.tipoCurso = tipoCurso;
    }

    public Curso() {

    }

    String registrarCurso(Curso curso){
         return "Curso em registramento";
    }

    String acessarCurso(Curso curso){
        return "Curso acessado";
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

    public String getTipoAssinatura() {
        return tipoAssinatura;
    }

    public void setTipoAssinatura(String tipoAssinatura) {
        this.tipoAssinatura = tipoAssinatura;
    }

    public TipoCurso getTipoCurso() {
        return tipoCurso;
    }

    public void setTipoCurso(TipoCurso tipoCurso) {
        this.tipoCurso = tipoCurso;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Curso curso = (Curso) o;
        return Objects.equals(id, curso.id) && Objects.equals(nome, curso.nome) && Objects.equals(tipoAssinatura, curso.tipoAssinatura) && tipoCurso == curso.tipoCurso;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome, tipoAssinatura);
    }

    @Override
    public String toString() {
        return "Curso{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", tipoAssinatura='" + tipoAssinatura + '\'' +
                ", tipoCurso=" + tipoCurso +
                '}';
    }
}
