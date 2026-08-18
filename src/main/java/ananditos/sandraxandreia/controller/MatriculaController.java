package ananditos.sandraxandreia.controller;

import ananditos.sandraxandreia.domain.matricula.StatusMatricula;
import ananditos.sandraxandreia.domain.usuario.Usuario;
import ananditos.sandraxandreia.domain.usuario.UsuarioCargo;
import ananditos.sandraxandreia.dto.request.MatriculaRequestDTO;
import ananditos.sandraxandreia.dto.response.MatriculaResponseDTO;
import ananditos.sandraxandreia.service.MatriculaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public MatriculaResponseDTO criar(@Valid @RequestBody MatriculaRequestDTO matricula,
                                      @AuthenticationPrincipal Usuario usuario) {
        return service.criar(matricula, usuario.getId());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CURADOR','ADMIN')")
    @Operation(summary = "Lista todas as matriculas")
    public List<MatriculaResponseDTO> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ALUNO')")
    @Operation(summary = "Lista as matriculas do aluno autenticado")
    public List<MatriculaResponseDTO> listarMinhasMatriculas(@AuthenticationPrincipal Usuario usuario) {
        return service.listarPorAluno(usuario.getId());
    }

    @GetMapping("/aluno/{alunoId}")
    @PreAuthorize("hasAnyRole('PROFESSOR','CURADOR','ADMIN')")
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
    @PreAuthorize("hasAnyRole('PROFESSOR','CURADOR','ADMIN')")
    @Operation(summary = "Busca uma matricula pelo id")
    public MatriculaResponseDTO buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ALUNO')")
    @Operation(summary = "Atualiza uma matricula existente")
    public MatriculaResponseDTO atualizar(@PathVariable Long id, @Valid @RequestBody MatriculaRequestDTO matricula,
                                          @AuthenticationPrincipal Usuario usuario) {
        return service.atualizar(id, matricula, usuario.getId());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ALUNO','CURADOR','ADMIN')")
    @Operation(summary = "Atualiza apenas o status de uma matricula")
    public MatriculaResponseDTO atualizarStatus(@PathVariable Long id, @RequestParam StatusMatricula novoStatus,
                                                @AuthenticationPrincipal Usuario usuario) {
        if (usuario.getPerfil() == UsuarioCargo.ALUNO) {
            return service.atualizarStatusDoAluno(id, novoStatus, usuario.getId());
        }
        return service.atualizarStatus(id, novoStatus);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ALUNO')")
    @Operation(summary = "Remove uma matricula pelo id")
    public void deletar(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        service.deletar(id, usuario.getId());
    }
}
