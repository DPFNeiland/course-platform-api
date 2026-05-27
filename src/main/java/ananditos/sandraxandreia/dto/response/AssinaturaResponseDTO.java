package ananditos.sandraxandreia.dto.response;

import ananditos.sandraxandreia.domain.assinatura.PlanoAssinatura;

import java.util.List;

public class AssinaturaResponseDTO {

    private Long id;
    private String nome;
    private PlanoAssinatura assinatura;
    private double preco;
    private List<String> beneficios;

    public AssinaturaResponseDTO(Long id, String nome, PlanoAssinatura assinatura, double preco, List<String> beneficios) {
        this.id = id;
        this.nome = nome;
        this.assinatura = assinatura;
        this.preco = preco;
        this.beneficios = beneficios;
    }

    public AssinaturaResponseDTO() {
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public PlanoAssinatura getAssinatura() {
        return assinatura;
    }

    public double getPreco() {
        return preco;
    }

    public List<String> getBeneficios() {
        return beneficios;
    }
}
