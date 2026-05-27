package ananditos.sandraxandreia.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CursoMaterialLinkRequestDTO {

    @NotBlank(message = "Titulo do material obrigatorio")
    private String titulo;

    @NotBlank(message = "Link do material obrigatorio")
    @Pattern(regexp = "https?://.+", message = "Link do material deve comecar com http:// ou https://")
    private String url;

    public CursoMaterialLinkRequestDTO() {
    }

    public String getTitulo() {
        return titulo;
    }

    public String getUrl() {
        return url;
    }
}
