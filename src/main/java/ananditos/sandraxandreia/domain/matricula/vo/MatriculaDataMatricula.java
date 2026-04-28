package ananditos.sandraxandreia.domain.matricula.vo;


import ananditos.sandraxandreia.domain.usuario.vo.UsuarioDataNascimento;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


@Embeddable
public class MatriculaDataMatricula {

    @Column(name = "data_matricula", nullable = false)
    private String valor;
    private LocalDate data;

    protected MatriculaDataMatricula() {

    }

    public MatriculaDataMatricula(String valor) {
        String normalizado = valor == null ? null : valor.trim();
        if (normalizado == null || normalizado.isBlank()) {
            throw new IllegalArgumentException("Data de matrícula é obrigatória");
        }
        this.valor = normalizado;


        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("d/M/yyyy");

        if (!isDataValida(valor, formatador)) {
            throw new IllegalArgumentException("Data de matrícula com formato inválido");
        }

        data = LocalDate.parse(valor, formatador);
        LocalDate dataMinima = LocalDate.parse("01/01/1900", formatador);
        LocalDate dataAtual = LocalDate.now();

        if (data.isBefore(dataMinima) ||     data.isAfter(dataAtual)) {
            throw new IllegalArgumentException("Data de matrícula inválida");
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

}
