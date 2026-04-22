package ananditos.sandraxandreia.domain;

import ananditos.sandraxandreia.domain.vo.SenhaCriptografada;
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

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Embedded
    private SenhaCriptografada senha;

    public Usuario() {
        // Construtor padrao exigido pela JPA.
    }

    public Usuario(Long id, String nome, String email, String senhaCriptografada) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = new SenhaCriptografada(senhaCriptografada);
    }

    public Long getId() {
        return id;
    }


    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha.getValor();
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setSenha(SenhaCriptografada senha) {
        this.senha = senha;
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
                '}';
    }
}

