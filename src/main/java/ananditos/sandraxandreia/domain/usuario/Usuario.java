package ananditos.sandraxandreia.domain.usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import ananditos.sandraxandreia.domain.usuario.vo.UsuarioCpf;
import ananditos.sandraxandreia.domain.usuario.vo.UsuarioDataNascimento;
import ananditos.sandraxandreia.domain.usuario.vo.UsuarioEmail;
import ananditos.sandraxandreia.domain.usuario.vo.UsuarioSenhaCriptografada;
import jakarta.persistence.*;
import jdk.jshell.spi.ExecutionControl;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Classe de dominio anotada como Entity.
 * Nesta primeira versao, ela já e suficiente para o Hibernate/JPA
 * gerar a tabela automaticamente no H2, mesmo sem Repository.
 */
@Entity
@Table(name = "usuario")
@Inheritance(strategy = InheritanceType.JOINED)
public class Usuario  implements UserDetails {

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

    @Embedded
    private UsuarioDataNascimento dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GeneroUsuario genero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UsuarioCargo cargo;

    public Usuario() {
        // Construtor padrao exigido pela JPA.
    }

    public Usuario(Long id, String nome, String email, String cpf, String senha, String dataNascimento, GeneroUsuario genero, UsuarioCargo cargo) {
        this.id = id;
        this.nome = nome;
        this.email = new UsuarioEmail(email);
        this.cpf = new UsuarioCpf(cpf);
        this.senha = new UsuarioSenhaCriptografada(senha);
        this.dataNascimento = new UsuarioDataNascimento(dataNascimento);
        this.genero = genero;
        this.cargo = cargo;
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

    public UsuarioDataNascimento getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(UsuarioDataNascimento dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public GeneroUsuario getGenero() {
        return genero;
    }

    public void setGenero(GeneroUsuario genero) {
        this.genero = genero;
    }

    public UsuarioCargo getCargo() {
        return cargo;
    }

    public void setCargo(UsuarioCargo cargo) {
        this.cargo = cargo;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id) && Objects.equals(nome, usuario.nome) && Objects.equals(email, usuario.email) && Objects.equals(cpf, usuario.cpf) && Objects.equals(senha, usuario.senha) && Objects.equals(dataNascimento, usuario.dataNascimento) && genero == usuario.genero && cargo == usuario.cargo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome, email, cpf, senha, dataNascimento, genero, cargo);
    }


    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", email=" + email +
                ", cpf=" + cpf +
                ", senha=" + senha +
                ", dataNascimento=" + dataNascimento +
                ", genero=" + genero +
                ", cargo=" + cargo +
                '}';
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.cargo == UsuarioCargo.ADMIN)
            return List.of(
                    new SimpleGrantedAuthority("ADMIN"),
                    new SimpleGrantedAuthority("PROFESSSOR"),
                    new SimpleGrantedAuthority("ALUNO"),
                    new SimpleGrantedAuthority("CURADOR"));

        if (this.cargo == UsuarioCargo.PROFESSOR)
            return List.of(new SimpleGrantedAuthority("PROFESSOR"));

        if (this.cargo == UsuarioCargo.ALUNO)
            return List.of(new SimpleGrantedAuthority("ALUNO"));

        if (this.cargo == UsuarioCargo.CURADOR)
            return List.of(new SimpleGrantedAuthority("CURADOR"));

        if (this.cargo == UsuarioCargo.ANONIMO)
            return List.of(new SimpleGrantedAuthority("ANONIMO"));

        return List.of();
    }

    @Override
    public @Nullable String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return this.email.getValor();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}

