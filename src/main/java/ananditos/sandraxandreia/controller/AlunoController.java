package ananditos.sandraxandreia.controller;

import ananditos.sandraxandreia.dto.*;
import ananditos.sandraxandreia.service.AlunoService;
import ananditos.sandraxandreia.service.ProfessorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/aluno")
@Tag(name = "Aluno", description = "API REST de aluno")
public class AlunoController {
        private final AlunoService service;

        public AlunoController(AlunoService service) {
            this.service = service;

        }

        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        @Operation(summary = "Cadastra um novo aluno")
        public AlunoResponseDTO criar(@RequestBody AlunoRequestDTO aluno)
        {
            return service.criar(aluno);
        }

        @GetMapping
        @Operation(summary = "Lista todos os alunos")
        public List<AlunoResponseDTO> listarTodos() {

            return service.listarTodos();
        }

        @GetMapping("/{id}")
        @Operation(summary = "Busca um aluno pelo id")
        public AlunoResponseDTO buscarPorId(@PathVariable Long id) {

            return service.buscarPorId(id);
        }

        @PutMapping("/{id}")
        @Operation(summary = "Atualiza um alunon existente")
        public AlunoResponseDTO atualizar(@PathVariable Long id, @RequestBody AlunoRequestDTO aluno) {
            return service.atualizar(id, aluno);
        }

        @DeleteMapping("/{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        @Operation(summary = "Remove um aluno pelo id")
        public void deletar(@PathVariable Long id) {
            service.deletar(id);
        }
}
