package ananditos.sandraxandreia.controller;

import ananditos.sandraxandreia.dto.request.MatriculaRequestDTO;
import ananditos.sandraxandreia.dto.response.MatriculaResponseDTO;
import ananditos.sandraxandreia.service.MatriculaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/matricula")
@Tag(name = "Matricula", description = "API REST de matricula")
public class MatriculaController {
    private final MatriculaService service;

    public MatriculaController(MatriculaService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra uma nova matricula")
    public MatriculaResponseDTO criar(@RequestBody MatriculaRequestDTO matricula) {
        return service.criar(matricula);
    }

    @GetMapping
    @Operation(summary = "Lista todos as matriculas")
    public List<MatriculaResponseDTO> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma matricula pelo id")
    public MatriculaResponseDTO buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma matricula existente")
    public MatriculaResponseDTO atualizar(@PathVariable Long id, @RequestBody MatriculaRequestDTO matricula) {
        return service.atualizar(id, matricula);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove uma matricula pelo id")
    public void deletar(@PathVariable Long id) {
        service.deletar(id);
    }

}
