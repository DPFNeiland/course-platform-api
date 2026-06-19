package ananditos.sandraxandreia.domain.usuario;

import ananditos.sandraxandreia.domain.usuario.vo.UsuarioCpf;
import ananditos.sandraxandreia.domain.usuario.vo.UsuarioDataNascimento;
import ananditos.sandraxandreia.domain.usuario.vo.UsuarioEmail;
import ananditos.sandraxandreia.domain.usuario.vo.UsuarioSenhaCriptografada;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
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
    @Column
    private UsuarioCargo perfil;

    public Usuario() {
        // Construtor padrao exigido pela JPA.
    }

    public Usuario(Long id, String nome, String email, String senhaCriptografada, String cpf, GeneroUsuario genero, String dataNascimento, UsuarioCargo perfil) {
        this.id = id;
        this.nome = nome;
        this.email = new UsuarioEmail(email);
        this.cpf = new UsuarioCpf(cpf);
        this.senha = new UsuarioSenhaCriptografada(senhaCriptografada);
        this.dataNascimento = new UsuarioDataNascimento(dataNascimento);
        this.genero = genero;
        this.perfil = perfil;

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

    public UsuarioCargo getPerfil() {
        return perfil;
    }

    public void setPerfil(UsuarioCargo perfil) {
        this.perfil = perfil;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id) && Objects.equals(nome, usuario.nome) && Objects.equals(email, usuario.email) && Objects.equals(cpf, usuario.cpf) && Objects.equals(senha, usuario.senha) && Objects.equals(dataNascimento, usuario.dataNascimento) && genero == usuario.genero && perfil == usuario.perfil;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome, email, cpf, senha, dataNascimento, genero, perfil);
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
                ", perfil=" + perfil +
                '}';
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.perfil == UsuarioCargo.ADMIN)
            return List.of(
                    new SimpleGrantedAuthority(UsuarioCargo.ADMIN.getRole()),
                    new SimpleGrantedAuthority(UsuarioCargo.PROFESSOR.getRole()),
                    new SimpleGrantedAuthority(UsuarioCargo.ALUNO.getRole()),
                    new SimpleGrantedAuthority(UsuarioCargo.CURADOR.getRole()));

        if (this.perfil == UsuarioCargo.PROFESSOR)
            return List.of(new SimpleGrantedAuthority(UsuarioCargo.PROFESSOR.getRole()));

        if (this.perfil == UsuarioCargo.ALUNO)
            return List.of(new SimpleGrantedAuthority(UsuarioCargo.ALUNO.getRole()));

        if (this.perfil == UsuarioCargo.CURADOR)
            return List.of(new SimpleGrantedAuthority(UsuarioCargo.CURADOR.getRole()));

        if (this.perfil == UsuarioCargo.ANONIMO)
            return List.of(new SimpleGrantedAuthority(UsuarioCargo.ANONIMO.getRole()));

        return List.of();
    }

    @Override
    public String getPassword() {
        return senha.getValor();
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

