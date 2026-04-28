package ananditos.sandraxandreia.domain.matricula.vo;


import ananditos.sandraxandreia.domain.usuario.vo.UsuarioDataNascimento;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


@Embeddable
public class MatriculaDataMatricula {

    @Column(name = "data_matricula", nullable = false)
    private LocalDate data;

    protected MatriculaDataMatricula() {

    }

    public MatriculaDataMatricula(LocalDate data) {
        this.data = data;
    }


    public static MatriculaDataMatricula agora() {
        return new MatriculaDataMatricula(LocalDate.now());
    }

    public LocalDate getData() {
        return data;
    }

    @Override
    public String toString() {
        return "" + data;
    }
}
