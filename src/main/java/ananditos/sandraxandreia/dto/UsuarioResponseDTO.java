package ananditos.sandraxandreia.dto;

import ananditos.sandraxandreia.domain.GeneroUsuario;

import java.time.LocalDate;

public class UsuarioResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private String cpf;
    private GeneroUsuario genero;
    private LocalDate dataNascimento;

    public UsuarioResponseDTO() {
    }

    public UsuarioResponseDTO(Long id, String nome, String email, String cpf, GeneroUsuario genero, LocalDate dataNascimento) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.genero = genero;
        this.dataNascimento = dataNascimento;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getCpf() { return cpf; }
    public GeneroUsuario getGenero() { return genero; }
    public LocalDate getDataNascimento() {return dataNascimento;}

}
