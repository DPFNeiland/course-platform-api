package ananditos.sandraxandreia.dto.response;

import ananditos.sandraxandreia.domain.usuario.UsuarioCargo;

public class LoginResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private UsuarioCargo cargo;
    private String perfil;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(Long id, String nome, String email, UsuarioCargo cargo, String perfil) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.cargo = cargo;
        this.perfil = perfil;
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

    public UsuarioCargo getCargo() {
        return cargo;
    }

    public String getPerfil() {
        return perfil;
    }
}
