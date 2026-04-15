package ananditos.sandraxandreia.domain;

import java.util.List;
import java.util.Objects;

public class Assinatura {

    private String nome;
    private PlanoAssinatura assinatura;
    private double preco;
    private List<String> beneficios;

    public Assinatura(String nome, PlanoAssinatura assinatura, double preco, List<String> beneficios) {
        this.nome = nome;
        this.assinatura = assinatura;
        this.preco = preco;
        this.beneficios = beneficios;
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

    public void setBeneficios(List<String> beneficios) {
        this.beneficios = beneficios;
    }

    @Override
    public String toString() {
        return "Assinatura{" +
                "nome='" + nome + '\'' +
                ", assinatura=" + assinatura +
                ", preco=" + preco +
                ", beneficios=" + beneficios +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Assinatura that = (Assinatura) o;
        return Double.compare(preco, that.preco) == 0 && Objects.equals(nome, that.nome) && assinatura == that.assinatura && Objects.equals(beneficios, that.beneficios);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome, assinatura, preco, beneficios);
    }
}
