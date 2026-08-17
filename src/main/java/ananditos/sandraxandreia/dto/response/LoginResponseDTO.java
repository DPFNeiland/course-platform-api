package ananditos.sandraxandreia.dto.response;

import java.time.Instant;

public class LoginResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private String cargo;
    private String perfil;
    private Instant expiraEm;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(Long id, String nome, String email, String cargo, String perfil,
                            Instant expiraEm) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.cargo = cargo;
        this.perfil = perfil;
        this.expiraEm = expiraEm;
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

    public Instant getExpiraEm() {
        return expiraEm;
    }
}
