package ananditos.sandraxandreia.domain;

import jakarta.persistence.*;

import java.util.List;
import java.util.Objects;

@Entity
@Table(name="assinatura")
public class Assinatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 100)
    private PlanoAssinatura assinatura;

    @Column(nullable = false, length = 100)
    private double preco;

    @Column(nullable = false, length = 100)
    private List<String> beneficios;

    public Assinatura(Long id, String nome, PlanoAssinatura assinatura, double preco, List<String> beneficios) {
        this.id = id;
        this.nome = nome;
        this.assinatura = assinatura;
        this.preco = preco;
        this.beneficios = beneficios;
    }

    public Assinatura(){

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

    public PlanoAssinatura getAssinatura() {
        return assinatura;
    }

    public void setAssinatura(PlanoAssinatura assinatura) {
        this.assinatura = assinatura;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public List<String> getBeneficios() {
        return beneficios;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Assinatura that = (Assinatura) o;
        return Double.compare(preco, that.preco) == 0 && Objects.equals(id, that.id) && Objects.equals(nome, that.nome) && assinatura == that.assinatura && Objects.equals(beneficios, that.beneficios);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome, assinatura, preco, beneficios);
    }

    public void setBeneficios(List<String> beneficios) {
        this.beneficios = beneficios;
    }

    @Override
    public String toString() {
        return "Assinatura{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", assinatura=" + assinatura +
                ", preco=" + preco +
                ", beneficios=" + beneficios +
                '}';
    }
}
