package ananditos.sandraxandreia.dto.request;

import ananditos.sandraxandreia.domain.assinatura.PlanoAssinatura;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

public class AssinaturaRequestDTO {

    @NotBlank(message = "Nome da assinatura obrigatorio")
    private String nome;

    @NotNull(message = "Plano da assinatura obrigatorio")
    private PlanoAssinatura assinatura;

    @PositiveOrZero(message = "Preco da assinatura invalido")
    private double preco;

    @NotEmpty(message = "A assinatura precisa ter ao menos um beneficio")
    private List<String> beneficios;

    public AssinaturaRequestDTO() {
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
