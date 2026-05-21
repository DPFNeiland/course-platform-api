package ananditos.sandraxandreia.dto.response;

import ananditos.sandraxandreia.domain.curso.TipoMaterialCurso;

import java.time.LocalDateTime;

public class CursoMaterialResponseDTO {

    private Long id;
    private Long cursoId;
    private String titulo;
    private TipoMaterialCurso tipo;
    private String url;
    private String nomeArquivo;
    private String contentType;
    private LocalDateTime dataCadastro;

    public CursoMaterialResponseDTO(Long id, Long cursoId, String titulo, TipoMaterialCurso tipo, String url, String nomeArquivo, String contentType, LocalDateTime dataCadastro) {
        this.id = id;
        this.cursoId = cursoId;
        this.titulo = titulo;
        this.tipo = tipo;
        this.url = url;
        this.nomeArquivo = nomeArquivo;
        this.contentType = contentType;
        this.dataCadastro = dataCadastro;
    }

    public CursoMaterialResponseDTO() {
    }

    public Long getId() {
        return id;
    }

    public Long getCursoId() {
        return cursoId;
    }

    public String getTitulo() {
        return titulo;
    }

    public TipoMaterialCurso getTipo() {
        return tipo;
    }

    public String getUrl() {
        return url;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public String getContentType() {
        return contentType;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }
}
