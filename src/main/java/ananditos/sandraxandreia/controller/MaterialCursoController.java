package ananditos.sandraxandreia.controller;

import ananditos.sandraxandreia.domain.curso.MaterialCurso;
import ananditos.sandraxandreia.dto.request.CursoMaterialLinkRequestDTO;
import ananditos.sandraxandreia.dto.response.CursoMaterialResponseDTO;
import ananditos.sandraxandreia.service.MaterialCursoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/curso/{cursoId}/materiais")
@Tag(name = "Material do Curso", description = "API REST para links e arquivos de aula")
public class MaterialCursoController {

    private final MaterialCursoService service;

    public MaterialCursoController(MaterialCursoService service) {
        this.service = service;
    }

    @PostMapping("/link")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PROFESSOR')")
    @Operation(summary = "Professor adiciona um link de aula em curso proprio")
    public CursoMaterialResponseDTO adicionarLink(@PathVariable Long cursoId,
                                                  @Valid @RequestBody CursoMaterialLinkRequestDTO material,
                                                  Authentication authentication) {
        return service.adicionarLink(cursoId, material, authentication.getName());
    }

    @PostMapping(value = "/arquivo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PROFESSOR')")
    @Operation(summary = "Professor envia um arquivo de aula em curso proprio")
    public CursoMaterialResponseDTO adicionarArquivo(@PathVariable Long cursoId,
                                                     @RequestParam String titulo,
                                                     @RequestParam("arquivo") MultipartFile arquivo,
                                                     Authentication authentication) {
        return service.adicionarArquivo(cursoId, titulo, arquivo, authentication.getName());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ALUNO','PROFESSOR','CURADOR','ADMIN')")
    @Operation(summary = "Lista os materiais de um curso")
    public List<CursoMaterialResponseDTO> listarPorCurso(@PathVariable Long cursoId) {
        return service.listarPorCurso(cursoId);
    }

    @GetMapping("/{materialId}/arquivo")
    @PreAuthorize("hasAnyRole('ALUNO','PROFESSOR','CURADOR','ADMIN')")
    @Operation(summary = "Baixa um arquivo de aula")
    public ResponseEntity<byte[]> baixarArquivo(@PathVariable Long cursoId, @PathVariable Long materialId) {
        MaterialCurso material = service.buscarArquivo(cursoId, materialId);

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (material.getContentType() != null && !material.getContentType().isBlank()) {
            try {
                mediaType = MediaType.parseMediaType(material.getContentType());
            } catch (InvalidMediaTypeException ignored) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(material.getNomeArquivo() == null ? "material.bin" : material.getNomeArquivo())
                                .build()
                                .toString())
                .body(material.getDadosArquivo());
    }
}
