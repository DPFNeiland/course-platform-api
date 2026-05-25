package ananditos.sandraxandreia.dto.response;

import ananditos.sandraxandreia.domain.usuario.GeneroUsuario;
import ananditos.sandraxandreia.domain.usuario.PerfilUsuario;

import java.time.LocalDate;

public class UsuarioResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private String cpf;
    private GeneroUsuario genero;
    private LocalDate dataNascimento;
    private PerfilUsuario perfil;

    public UsuarioResponseDTO() {
    }

    public UsuarioResponseDTO(Long id, String nome, String email, String cpf, GeneroUsuario genero, LocalDate dataNascimento) {
        this(id, nome, email, cpf, genero, dataNascimento, null);
    }

    public UsuarioResponseDTO(Long id, String nome, String email, String cpf, GeneroUsuario genero, LocalDate dataNascimento, PerfilUsuario perfil) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.genero = genero;
        this.dataNascimento = dataNascimento;
        this.perfil = perfil;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getCpf() { return cpf; }
    public GeneroUsuario getGenero() { return genero; }
    public LocalDate getDataNascimento() {return dataNascimento;}
    public PerfilUsuario getPerfil() { return perfil; }

}
