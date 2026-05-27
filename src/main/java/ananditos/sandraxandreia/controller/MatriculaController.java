package ananditos.sandraxandreia.controller;

import ananditos.sandraxandreia.domain.matricula.StatusMatricula;
import ananditos.sandraxandreia.dto.request.MatriculaRequestDTO;
import ananditos.sandraxandreia.dto.response.MatriculaResponseDTO;
import ananditos.sandraxandreia.service.MatriculaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('ALUNO')")
    @Operation(summary = "Cadastra uma nova matricula")
    public MatriculaResponseDTO criar(@Valid @RequestBody MatriculaRequestDTO matricula) {
        return service.criar(matricula);
    }

    @GetMapping
    @PreAuthorize("hasRole('ALUNO')")
    @Operation(summary = "Lista todas as matriculas")
    public List<MatriculaResponseDTO> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/aluno/{alunoId}")
    @PreAuthorize("hasAnyRole('ALUNO','PROFESSOR','CURADOR','ADMIN')")
    @Operation(summary = "Lista as matriculas de um aluno")
    public List<MatriculaResponseDTO> listarPorAluno(@PathVariable Long alunoId) {
        return service.listarPorAluno(alunoId);
    }

    @GetMapping("/curso/{cursoId}")
    @PreAuthorize("hasAnyRole('PROFESSOR','CURADOR','ADMIN')")
    @Operation(summary = "Lista as matriculas de um curso")
    public List<MatriculaResponseDTO> listarPorCurso(@PathVariable Long cursoId) {
        return service.listarPorCurso(cursoId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ALUNO','PROFESSOR','CURADOR','ADMIN')")
    @Operation(summary = "Busca uma matricula pelo id")
    public MatriculaResponseDTO buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ALUNO')")
    @Operation(summary = "Atualiza uma matricula existente")
    public MatriculaResponseDTO atualizar(@PathVariable Long id, @Valid @RequestBody MatriculaRequestDTO matricula) {
        return service.atualizar(id, matricula);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ALUNO','CURADOR','ADMIN')")
    @Operation(summary = "Atualiza apenas o status de uma matricula")
    public MatriculaResponseDTO atualizarStatus(@PathVariable Long id, @RequestParam StatusMatricula novoStatus) {
        return service.atualizarStatus(id, novoStatus);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ALUNO')")
    @Operation(summary = "Remove uma matricula pelo id")
    public void deletar(@PathVariable Long id) {
        service.deletar(id);
    }
}
