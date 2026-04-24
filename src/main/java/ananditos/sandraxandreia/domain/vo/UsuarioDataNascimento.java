package ananditos.sandraxandreia.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

@Embeddable
public class UsuarioDataNascimento {

    @Column(name = "data_nascimento", nullable = false)
    private String valor;
    private LocalDate data;

    protected UsuarioDataNascimento() {

    }

    public UsuarioDataNascimento(String valor) {
        String normalizado = valor == null ? null : valor.trim();
        if (normalizado == null || normalizado.isBlank()) {
            throw new IllegalArgumentException("Data de nascimento é obrigatória");
        }
        this.valor = normalizado;

        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        if (!isDataValida(valor, formatador)) {
            throw new IllegalArgumentException("Data de nascimento com formato inválido");
        }

        data = LocalDate.parse(valor, formatador);
        LocalDate dataMinima = LocalDate.parse("01/01/1900", formatador);
        LocalDate dataAtual = LocalDate.now();

        if (data.isBefore(dataMinima) && data.isAfter(dataAtual)) {
            throw new IllegalArgumentException("Data de nascimento inválida");
        }
    }

    public boolean isDataValida(String data, DateTimeFormatter formato) {
        try {
            LocalDate.parse(data, formato);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "UsuarioDataNascimento{" +
                "valor='" + valor + '\'' +
                ", dataNascimento=" + data +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UsuarioDataNascimento that = (UsuarioDataNascimento) o;
        return Objects.equals(valor, that.valor) && Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor, data);
    }
}
