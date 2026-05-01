package ananditos.sandraxandreia.controller;

import ananditos.sandraxandreia.domain.curso.StatusCurso;
import ananditos.sandraxandreia.dto.request.CursoRequestDTO;
import ananditos.sandraxandreia.dto.response.CursoResponseDTO;
import ananditos.sandraxandreia.service.CursoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
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
        @Operation(summary = "Cadastra um novo curso")
        public CursoResponseDTO criar(@RequestBody CursoRequestDTO curso) {
            return service.criar(curso);
        }

        @GetMapping
        @Operation(summary = "Lista todos os cursos")
        public List<CursoResponseDTO> listarTodos() {
            return service.listarTodos();
        }

        @GetMapping("/{id}")
        @Operation(summary = "Busca um curso pelo id")
        public CursoResponseDTO buscarPorId(@PathVariable Long id) {
            return service.buscarPorId(id);
        }

        @PutMapping("/{id}")
        @Operation(summary = "Atualiza um curso existente")
        public CursoResponseDTO atualizar(@PathVariable Long id, @RequestBody CursoRequestDTO curso) {
            return service.atualizar(id, curso);
        }

        @PutMapping("/{id}/status")
        @Operation(summary = "Atualiza o status do curso")
        public CursoResponseDTO atualizar_status(@PathVariable Long id, @RequestParam StatusCurso novoStatus) {
                return service.atualizar_status(id, novoStatus);
        }

        @DeleteMapping("/{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        @Operation(summary = "Remove um curso pelo id")
        public void deletar(@PathVariable Long id) {
            service.deletar(id);
        }


}
