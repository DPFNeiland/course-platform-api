package ananditos.sandraxandreia.dto;

import ananditos.sandraxandreia.domain.GeneroUsuario;
import ananditos.sandraxandreia.domain.vo.UsuarioEmail;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public class UsuarioRequestDTO {
    @NotBlank(message = "Nome e obrigatorio")
    private String nome;

    @Email(message = "E-mail invalido")
    @NotBlank(message = "E-mail e obrigatorio")
    private String email;

    @NotBlank(message = "Cpf eh obrigaatorio")
    @Pattern(regexp = "\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", message = "CPF deve ter 11 digitos ou estar no formato 000.000.000-00")
    private String cpf;

    @NotBlank(message = "Senha e obrigatoria")
    private String senha;

    @NotBlank(message = "Genero eh obrigatorio")
    private GeneroUsuario genero;

    @NotBlank(message = "Data de nascimento é obrigatória")
    private LocalDate dataNascimento;

    public UsuarioRequestDTO() {
    }

    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenha() { return senha; }
    public String getCpf() { return cpf; }
    public GeneroUsuario getGenero() { return genero; }
    public LocalDate getDataNascimento() {return dataNascimento; }

}
