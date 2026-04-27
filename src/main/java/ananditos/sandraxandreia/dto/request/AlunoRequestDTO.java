package ananditos.sandraxandreia.dto.request;


import ananditos.sandraxandreia.domain.StatusAluno;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class AlunoRequestDTO extends UsuarioRequestDTO {

    @NotBlank(message = "RA nao preenchido")
    @Pattern(regexp = "[A-Za-z]{2}\\d{4}", message = "RA deve seguir o formato AB1234")
    private String ra;

    @NotNull(message = "Status do aluno não preenchido")
    private StatusAluno status;

    public AlunoRequestDTO() {
    }


    public String getRa() {
        return ra;
    }
    public StatusAluno getStatus() {
        return status;
    }
}
