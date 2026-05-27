package ananditos.sandraxandreia.controller;

import ananditos.sandraxandreia.dto.request.ProfessorRequestDTO;
import ananditos.sandraxandreia.dto.response.ProfessorResponseDTO;
import ananditos.sandraxandreia.service.ProfessorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    @PreAuthorize("hasRole('PROFESSOR')")
    @Operation(summary = "Lista todos os professor")
    public List<ProfessorResponseDTO> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PROFESSOR')")
    @Operation(summary = "Busca um professor pelo id")
    public ProfessorResponseDTO buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROFESSOR')")
    @Operation(summary = "Atualiza um professor existente")
    public ProfessorResponseDTO atualizar(@PathVariable Long id, @Valid @RequestBody ProfessorRequestDTO professor) {
        return service.atualizar(id, professor);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('PROFESSOR')")
    @Operation(summary = "Remove um professor pelo id")
    public void deletar(@PathVariable Long id) {
        service.deletar(id);
    }
}
