package ananditos.sandraxandreia.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UsuarioRequestDTO {
    @NotBlank(message = "Nome e obrigatorio")
    private String nome;

    @Email(message = "E-mail invalido")
    @NotBlank(message = "E-mail e obrigatorio")
    private String email;

    @NotBlank(message = "Senha e obrigatoria")
    private String senha;

    public UsuarioRequestDTO() {
    }

    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenha() { return senha; }

}
