package ananditos.sandraxandreia.dto.response;

import ananditos.sandraxandreia.domain.usuario.GeneroUsuario;
import ananditos.sandraxandreia.domain.usuario.UsuarioCargo;

import java.time.LocalDate;

public class UsuarioResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private String cpf;
    private GeneroUsuario genero;
    private LocalDate dataNascimento;
    private UsuarioCargo cargo;

    public UsuarioResponseDTO() {
    }

    public UsuarioResponseDTO(Long id, String nome, String email, String cpf, GeneroUsuario genero, LocalDate dataNascimento, UsuarioCargo cargo) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.genero = genero;
        this.dataNascimento = dataNascimento;
        this.cargo = cargo;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getCpf() { return cpf; }
    public GeneroUsuario getGenero() { return genero; }
    public LocalDate getDataNascimento() {return dataNascimento;}
    public UsuarioCargo getCargo() {
        return cargo;
    }
}
