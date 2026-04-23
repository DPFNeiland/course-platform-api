package ananditos.sandraxandreia.dto;

import ananditos.sandraxandreia.domain.GeneroUsuario;

public class UsuarioResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private String cpf;
    private GeneroUsuario genero;

    public UsuarioResponseDTO() {
    }

    public UsuarioResponseDTO(Long id, String nome, String email, String cpf , GeneroUsuario genero) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.genero = genero;
    }
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getCpf() { return cpf; }
    public GeneroUsuario getGenero() { return genero; }

}
