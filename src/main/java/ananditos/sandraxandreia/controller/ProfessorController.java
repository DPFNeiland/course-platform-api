package ananditos.sandraxandreia.controller;

import ananditos.sandraxandreia.dto.ProfessorRequestDTO;
import ananditos.sandraxandreia.dto.ProfessorResponseDTO;
import ananditos.sandraxandreia.dto.UsuarioResponseDTO;
import ananditos.sandraxandreia.service.ProfessorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
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
    @Operation(summary = "Cadastra um novo professor")
    public ProfessorResponseDTO criar(@RequestBody ProfessorRequestDTO professor)
    {
        return service.criar(professor);
    }

    @GetMapping
    @Operation(summary = "Lista todos os professor")
    public List<ProfessorResponseDTO> listarTodos() {

        return service.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um professor pelo id")
    public ProfessorResponseDTO buscarPorId(@PathVariable Long id) {

        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um professor existente")
    public ProfessorResponseDTO atualizar(@PathVariable Long id, @RequestBody ProfessorRequestDTO professor) {
        return service.atualizar(id, professor);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove um professor pelo id")
    public void deletar(@PathVariable Long id) {
        service.deletar(id);
    }
}
