package ananditos.sandraxandreia.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "matricula")
public class Matricula {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private StatusMatricula status;

    public void manterMatricula(){
        System.out.println( "Matricula mantida");
    };

    public void matricular(Aluno aluno) {
        System.out.println(" Pessoa matriculada");
    };


}
