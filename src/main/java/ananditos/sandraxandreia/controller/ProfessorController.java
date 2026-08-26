package ananditos.sandraxandreia.controller;

import ananditos.sandraxandreia.dto.request.ProfessorRequestDTO;
import ananditos.sandraxandreia.dto.response.ProfessorResponseDTO;
import ananditos.sandraxandreia.service.ProfessorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ananditos.sandraxandreia.domain.usuario.Usuario;
import ananditos.sandraxandreia.domain.usuario.UsuarioCargo;

import java.util.List;

@RestController
@RequestMapping("/professor")
@Tag(name = "Professor", description = "API REST de professor")
public class ProfessorController {
    private final ProfessorService service;

    public ProfessorController(ProfessorService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("permitAll()")
    @Operation(summary = "Cadastra um novo professor")
    public ProfessorResponseDTO criar(@Valid @RequestBody ProfessorRequestDTO professor) {
        return service.criar(professor);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CURADOR','ADMIN')")
    @Operation(summary = "Lista todos os professor")
    public List<ProfessorResponseDTO> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROFESSOR','CURADOR','ADMIN')")
    @Operation(summary = "Busca um professor pelo id")
    public ProfessorResponseDTO buscarPorId(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        validarAcessoProprio(id, usuario);
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROFESSOR')")
    @Operation(summary = "Atualiza um professor existente")
    public ProfessorResponseDTO atualizar(@PathVariable Long id, @Valid @RequestBody ProfessorRequestDTO professor,
                                          @AuthenticationPrincipal Usuario usuario) {
        validarAcessoProprio(id, usuario);
        return service.atualizar(id, professor);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('PROFESSOR')")
    @Operation(summary = "Remove um professor pelo id")
    public void deletar(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        validarAcessoProprio(id, usuario);
        service.deletar(id);
    }

    private void validarAcessoProprio(Long id, Usuario usuario) {
        if (usuario.getPerfil() == UsuarioCargo.PROFESSOR && !usuario.getId().equals(id)) {
            throw new AccessDeniedException("Professor nao pode acessar dados de outro professor");
        }
    }
}
