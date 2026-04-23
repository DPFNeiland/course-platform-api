package ananditos.sandraxandreia.controller;


import ananditos.sandraxandreia.domain.Curso;
import ananditos.sandraxandreia.service.CursoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequestMapping("/curso")
@Tag(name = "curso", description = "API REST de curso")
public class CursoController {

        private final CursoService service;

        public CursoController(CursoService service) {
                this.service = service;
        }

        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        @Operation(summary = "Cadastra um novo curso")
        public Curso criar(@RequestBody Curso curso) {
            return service.criar(curso);
        }

        @GetMapping
        @Operation(summary = "Lista todos os cursos")
        public List<Curso> listarTodos() {
            return service.listarTodos();
        }

        @GetMapping("/{id}")
        @Operation(summary = "Busca um curso pelo id")
        public Curso buscarPorId(@PathVariable Long id) {
            return service.buscarPorId(id);
        }

        @PutMapping("/{id}")
        @Operation(summary = "Atualiza um curso existente")
        public Curso atualizar(@PathVariable Long id, @RequestBody Curso curso) {
            return service.atualizar(id, curso);
        }

        @DeleteMapping("/{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        @Operation(summary = "Remove um curso pelo id")
        public void deletar(@PathVariable Long id) {
            service.deletar(id);
        }


}
