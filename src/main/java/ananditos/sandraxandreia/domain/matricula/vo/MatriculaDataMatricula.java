package ananditos.sandraxandreia.domain.matricula.vo;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;


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
