package ananditos.sandraxandreia.domain;

import jakarta.persistence.*;

@Entity
@Table(name="aluno")
public class Aluno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private int RA;

    @Column(nullable = false, length = 100)
    private String planoAss;

    @Column(nullable = false, length = 100)
    private String respFinanceiro;

    String escreverTopico(String mensagem) {
        return ("Mensagem: " + mensagem);
    }
}
