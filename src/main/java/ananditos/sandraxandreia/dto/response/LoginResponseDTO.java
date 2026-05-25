package ananditos.sandraxandreia.dto.response;

public class LoginResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private String cargo;
    private String perfil;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(Long id, String nome, String email, String cargo, String perfil) {
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

    public String getCargo() {
        return cargo;
    }

    public String getPerfil() {
        return perfil;
    }
}
