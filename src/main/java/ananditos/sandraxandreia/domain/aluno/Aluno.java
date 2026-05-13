package ananditos.sandraxandreia.domain.aluno;

import ananditos.sandraxandreia.domain.matricula.Matricula;
import ananditos.sandraxandreia.domain.usuario.GeneroUsuario;
import ananditos.sandraxandreia.domain.usuario.Usuario;
import ananditos.sandraxandreia.domain.aluno.vo.AlunoRA;
import ananditos.sandraxandreia.domain.usuario.UsuarioCargo;
import ananditos.sandraxandreia.domain.usuario.vo.UsuarioSenhaCriptografada;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name="aluno")
public class Aluno extends Usuario {

    @Embedded
    private AlunoRA ra;

    @Enumerated(EnumType.STRING)
    private StatusAluno status;

    @OneToMany(mappedBy = "aluno", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Matricula> cursos = new ArrayList<>();

    public Aluno() {
        // Pro JPA
    }

    public Aluno(Long id, String nome, String email, String cpf, String senha, String dataNascimento, GeneroUsuario genero, UsuarioCargo cargo, AlunoRA ra, StatusAluno status, List<Matricula> cursos) {
        super(id, nome, email, cpf, senha, dataNascimento, genero, UsuarioCargo.ALUNO);
        this.ra = ra;
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

    public List<Matricula> getCursos() {
        return cursos;
    }

    public void setCursos(List<Matricula> cursos) {
        this.cursos = cursos;
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
