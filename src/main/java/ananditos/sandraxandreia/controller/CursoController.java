package ananditos.sandraxandreia.controller;

import ananditos.sandraxandreia.domain.curso.StatusCurso;
import ananditos.sandraxandreia.dto.request.CursoRequestDTO;
import ananditos.sandraxandreia.dto.response.CursoResponseDTO;
import ananditos.sandraxandreia.service.CursoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/curso")
@Tag(name = "Curso", description = "API REST de curso")
public class CursoController {

    private final CursoService service;

    public CursoController(CursoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PROFESSOR')")
    @Operation(summary = "Cadastra um novo curso")
    public CursoResponseDTO criar(@Valid @RequestBody CursoRequestDTO curso) {
        return service.criar(curso);
    }

    @GetMapping
    @PreAuthorize("hasRole('PROFESSOR')")
    @Operation(summary = "Lista todos os cursos")
    public List<CursoResponseDTO> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/disponiveis")
    @PreAuthorize("hasAnyRole('ALUNO','PROFESSOR','CURADOR','ADMIN')")
    @Operation(summary = "Lista os cursos aprovados e disponiveis para matricula")
    public List<CursoResponseDTO> listarDisponiveis() {
        return service.listarDisponiveis();
    }

    @GetMapping("/professor/{professorId}")
    @PreAuthorize("hasAnyRole('PROFESSOR','CURADOR','ADMIN')")
    @Operation(summary = "Lista todos os cursos de um professor")
    public List<CursoResponseDTO> listarPorProfessor(@PathVariable Long professorId) {
        return service.listarPorProfessor(professorId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ALUNO','PROFESSOR','CURADOR','ADMIN')")
    @Operation(summary = "Busca um curso pelo id")
    public CursoResponseDTO buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @GetMapping({"/EmAndamento", "/status"})
    @PreAuthorize("hasAnyRole('PROFESSOR','CURADOR','ADMIN')")
    @Operation(summary = "Busca cursos por status")
    public List<CursoResponseDTO> listarPorStatus(@RequestParam(required = false) StatusCurso status) {
        if (status != null) {
            return service.listarPorStatus(status);
        }

        return service.listarTodos();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROFESSOR')")
    @Operation(summary = "Atualiza um curso existente")
    public CursoResponseDTO atualizar(@PathVariable Long id, @Valid @RequestBody CursoRequestDTO curso) {
        return service.atualizar(id, curso);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('CURADOR','ADMIN')")
    @Operation(summary = "Atualiza o status do curso")
    public CursoResponseDTO atualizarStatus(@PathVariable Long id, @RequestParam StatusCurso novoStatus) {
        return service.atualizar_status(id, novoStatus);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('PROFESSOR')")
    @Operation(summary = "Remove um curso pelo id")
    public void deletar(@PathVariable Long id) {
        service.deletar(id);
    }
}
