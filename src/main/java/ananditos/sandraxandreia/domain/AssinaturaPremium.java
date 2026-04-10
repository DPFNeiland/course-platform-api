package ananditos.sandraxandreia.domain;

import java.util.Objects;

public class AssinaturaPremium implements Planos{

    private String nome;
    private String tipo;
    private float preco;
    private String funcionalidades;

    public AssinaturaPremium(String nome, String tipo, float preco, String funcionalidades) {
        this.nome = nome;
        this.tipo = tipo;
        this.preco = preco;
        this.funcionalidades = funcionalidades;
    }

    public void cancelarAssinatura() {

    }

    public void mudarAssinatura() {

    }

    public void assinar() {

    }

    public void pagAuto() {

    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public float getPreco() {
        return preco;
    }

    public void setPreco(float preco) {
        this.preco = preco;
    }

    public String getFuncionalidades() {
        return funcionalidades;
    }

    public void setFuncionalidades(String funcionalidades) {
        this.funcionalidades = funcionalidades;
    }

    @Override
    public String toString() {
        return "AssinaturaPremium{" +
                "nome='" + nome + '\'' +
                ", tipo='" + tipo + '\'' +
                ", preco=" + preco +
                ", funcionalidades='" + funcionalidades + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AssinaturaPremium that = (AssinaturaPremium) o;
        return Float.compare(preco, that.preco) == 0 && Objects.equals(nome, that.nome) && Objects.equals(tipo, that.tipo) && Objects.equals(funcionalidades, that.funcionalidades);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome, tipo, preco, funcionalidades);
    }
}
