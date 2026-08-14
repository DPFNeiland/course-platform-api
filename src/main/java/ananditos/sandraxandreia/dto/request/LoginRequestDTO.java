package ananditos.sandraxandreia.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequestDTO {
    @Email(message = "E-mail invalido")
    @NotBlank(message = "E-mail e obrigatorio")
    private String email;

    @NotBlank(message = "Senha e obrigatoria")
    private String senha;

    public LoginRequestDTO() {
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }
}
