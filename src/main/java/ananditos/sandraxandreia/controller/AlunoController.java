// código documentado por Betina Volpi com o intuito de revisar a matéria,
// além de explicar como funciona para possíveis leitores que possam utilizá-lo
package ananditos.sandraxandreia.controller;

import ananditos.sandraxandreia.dto.request.AlunoRequestDTO;
import ananditos.sandraxandreia.dto.response.AlunoResponseDTO;
import ananditos.sandraxandreia.service.AlunoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // formata em JSON
@RequestMapping("/aluno") // endereço base da API
@Tag(name = "Aluno", description = "API REST de aluno")
public class AlunoController {
        private final AlunoService service;

        public AlunoController(AlunoService service) {
            this.service = service;

        }

        @PostMapping //criar
        @ResponseStatus(HttpStatus.CREATED)
        @Operation(summary = "Cadastra um novo aluno")

        public AlunoResponseDTO criar(
                // para o JSON enviado ser transformado em um AlunoRequestDTO
                // e, assim, validar as regras
                // @RequestBody -> usado no POST e PUT
                @RequestBody AlunoRequestDTO aluno)
        {
            return service.criar(aluno);
        }

        @GetMapping //busca todos
        @Operation(summary = "Lista todos os alunos")
        public List<AlunoResponseDTO> listarTodos() {

            return service.listarTodos();
        }

        @GetMapping("/{id}") //busca pelo id
        @Operation(summary = "Busca um aluno pelo id")
        public AlunoResponseDTO buscarPorId(@PathVariable Long id) {

            return service.buscarPorId(id);
        }

        @PutMapping("/{id}") //atualiza pelo id
        @Operation(summary = "Atualiza um aluno existente")
        public AlunoResponseDTO atualizar(@PathVariable Long id, @RequestBody AlunoRequestDTO aluno) {
            return service.atualizar(id, aluno);
        }

        @DeleteMapping("/{id}") //deleta pelo id
        @ResponseStatus(HttpStatus.NO_CONTENT) //não recebe nenhum JSON como resposta (deletou)
        @Operation(summary = "Remove um aluno pelo id")
        public void deletar(@PathVariable Long id) {
            service.deletar(id);
        }
}
