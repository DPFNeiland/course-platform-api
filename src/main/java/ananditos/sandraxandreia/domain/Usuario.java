package ananditos.sandraxandreia.domain;

import ananditos.sandraxandreia.domain.vo.UsuarioCpf;
import ananditos.sandraxandreia.domain.vo.UsuarioEmail;
import ananditos.sandraxandreia.domain.vo.UsuarioSenhaCriptografada;
import jakarta.persistence.*;

import java.util.Objects;

/**
 * Classe de dominio anotada como Entity.
 * Nesta primeira versao, ela já e suficiente para o Hibernate/JPA
 * gerar a tabela automaticamente no H2, mesmo sem Repository.
 */
@Entity
@Table(name = "usuario")
@Inheritance(strategy = InheritanceType.JOINED)
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Embedded
    private UsuarioEmail email;

    @Embedded
    private UsuarioCpf cpf;

    @Embedded
    private UsuarioSenhaCriptografada senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GeneroUsuario genero;


    public Usuario() {
        // Construtor padrao exigido pela JPA.
    }

    public Usuario(Long id, String nome, String email, String senhaCriptografada, String cpf, GeneroUsuario genero) {
        this.id = id;
        this.nome = nome;
        this.email = new UsuarioEmail(email);
        this.senha = new UsuarioSenhaCriptografada(senhaCriptografada);
        this.cpf = new UsuarioCpf(cpf);
        this.genero = genero;

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public UsuarioEmail getEmail() {
        return email;
    }

    public void setEmail(UsuarioEmail email) {
        this.email = email;
    }

    public UsuarioCpf getCpf() {
        return cpf;
    }

    public void setCpf(UsuarioCpf cpf) {
        this.cpf = cpf;
    }

    public UsuarioSenhaCriptografada getSenha() {
        return senha;
    }

    public void setSenha(UsuarioSenhaCriptografada senha) {
        this.senha = senha;
    }

    public GeneroUsuario getGenero() {
        return genero;
    }

    public void setGenero(GeneroUsuario genero) {
        this.genero = genero;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id) && Objects.equals(nome, usuario.nome) && Objects.equals(email, usuario.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome, email);
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", email='" + email + '\'' +
                ", cpf=" + cpf +
                '}';
    }
}

