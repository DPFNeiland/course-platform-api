package ananditos.sandraxandreia.domain;

import jakarta.persistence.*;

import java.util.Objects;

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
    
    public Aluno(Long id, int RA, String planoAss, String respFinanceiro) {
        this.id = id;
        this.RA = RA;
        this.planoAss = planoAss;
        this.respFinanceiro = respFinanceiro;
    }

    public Aluno() {
        
    }

    String escreverTopico(String mensagem) {
        return ("Mensagem: " + mensagem);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getRA() {
        return RA;
    }

    public void setRA(int RA) {
        this.RA = RA;
    }

    public String getPlanoAss() {
        return planoAss;
    }

    public void setPlanoAss(String planoAss) {
        this.planoAss = planoAss;
    }

    public String getRespFinanceiro() {
        return respFinanceiro;
    }

    public void setRespFinanceiro(String respFinanceiro) {
        this.respFinanceiro = respFinanceiro;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Aluno aluno = (Aluno) o;
        return RA == aluno.RA && Objects.equals(id, aluno.id) && Objects.equals(planoAss, aluno.planoAss) && Objects.equals(respFinanceiro, aluno.respFinanceiro);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, RA, planoAss, respFinanceiro);
    }
}
