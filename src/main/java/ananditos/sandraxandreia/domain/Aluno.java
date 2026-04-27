package ananditos.sandraxandreia.domain;

import ananditos.sandraxandreia.domain.vo.AlunoRA;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name="aluno")
public class Aluno extends Usuario{

    @Embedded
    private AlunoRA ra;

    @Enumerated(EnumType.STRING)
    private StatusAluno status;

    public Aluno() {
        // Pro JPA
    }

    public Aluno(Long id, String nome, String email, String senhaCriptografada, String cpf, GeneroUsuario genero, String dataNascimento, String ra, StatusAluno status) {
        super(id, nome, email, senhaCriptografada, cpf, genero, dataNascimento);
        this.ra = new AlunoRA(ra);
        this.status = status;
    }

    public AlunoRA getRa() {
        return ra;
    }

    public void setRa(AlunoRA ra) {
        this.ra = ra;
    }

    public StatusAluno getStatus() {
        return status;
    }

    public void setStatus(StatusAluno status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Aluno aluno = (Aluno) o;
        return Objects.equals(ra, aluno.ra) && status == aluno.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), ra, status);
    }

    @Override
    public String toString() {
        return "Aluno{" +
                "RA=" + ra +
                ", Status=" + status +
                '}';
    }
}
