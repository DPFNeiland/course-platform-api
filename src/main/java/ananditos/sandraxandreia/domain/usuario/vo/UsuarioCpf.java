package ananditos.sandraxandreia.domain.usuario.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class UsuarioCpf {

    @Column(name = "cpf", nullable = false, unique = true, length = 11)
    private String valor;

    protected UsuarioCpf() {

    }

    public UsuarioCpf(String valor) {
        String normalizado = valor == null ? null : valor.replaceAll("[^0-9]", "");
        if (normalizado == null || normalizado.isBlank()) {
            throw new IllegalArgumentException("CPF eh obrigatorio");
        }
        if (!isValido(normalizado)) {
            throw new IllegalArgumentException("CPF invalido");
        }
        this.valor = normalizado;
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UsuarioCpf that = (UsuarioCpf) o;
        return Objects.equals(valor, that.valor);
    }

    private boolean isValido(String cpf) {
        if (cpf.length() != 11) return false;
        if (cpf.chars().distinct().count() == 1) return false; // rejeita 111.111.111-11, etc

        // Valida primeiro dígito
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += (cpf.charAt(i) - '0') * (10 - i);
        }
        int primeiroDigito = 11 - (soma % 11);
        if (primeiroDigito >= 10) primeiroDigito = 0;
        if (primeiroDigito != (cpf.charAt(9) - '0')) return false;

        // Valida segundo dígito
        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += (cpf.charAt(i) - '0') * (11 - i);
        }
        int segundoDigito = 11 - (soma % 11);
        if (segundoDigito >= 10) segundoDigito = 0;
        return segundoDigito == (cpf.charAt(10) - '0');
    }
}
