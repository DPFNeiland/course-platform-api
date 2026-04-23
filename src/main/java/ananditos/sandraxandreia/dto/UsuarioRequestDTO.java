package ananditos.sandraxandreia.dto;

import ananditos.sandraxandreia.domain.GeneroUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UsuarioRequestDTO {
    @NotBlank(message = "Nome e obrigatorio")
    private String nome;

    @Email(message = "E-mail invalido")
    @NotBlank(message = "E-mail e obrigatorio")
    private String email;

    @NotBlank(message = "Cpf eh obrigaatorio")
    private String cpf;

    @NotBlank(message = "Senha e obrigatoria")
    private String senha;

    @NotBlank(message = "Genero eh obrigatorio")
    private GeneroUsuario genero;


    public UsuarioRequestDTO() {
    }

    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenha() { return senha; }
    public String getCpf() { return cpf; }
    public GeneroUsuario getGenero() { return genero; }

}
