// código documentado por Betina Volpi com o intuito de revisar a matéria,
// além de explicar como funciona para possíveis leitores que possam utilizá-lo
package ananditos.sandraxandreia.controller;

import ananditos.sandraxandreia.dto.request.AlunoRequestDTO;
import ananditos.sandraxandreia.dto.response.AlunoResponseDTO;
import ananditos.sandraxandreia.service.AlunoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ananditos.sandraxandreia.domain.usuario.Usuario;
import ananditos.sandraxandreia.domain.usuario.UsuarioCargo;

import java.util.List;

@RestController // formata em JSON
@RequestMapping("/aluno") // endereço base da API
@Tag(name = "Aluno", description = "API REST de aluno")
public class AlunoController {
    private final AlunoService service;

    public AlunoController(AlunoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("permitAll()")
    @Operation(summary = "Cadastra um novo aluno")
    public AlunoResponseDTO criar(@Valid @RequestBody AlunoRequestDTO aluno) {
        return service.criar(aluno);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CURADOR','ADMIN')")
    @Operation(summary = "Lista todos os alunos")
    public List<AlunoResponseDTO> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ALUNO','CURADOR','ADMIN')")
    @Operation(summary = "Busca um aluno pelo id")
    public AlunoResponseDTO buscarPorId(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        validarAcessoProprio(id, usuario);
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ALUNO')")
    @Operation(summary = "Atualiza um aluno existente")
    public AlunoResponseDTO atualizar(@PathVariable Long id, @Valid @RequestBody AlunoRequestDTO aluno,
                                      @AuthenticationPrincipal Usuario usuario) {
        validarAcessoProprio(id, usuario);
        return service.atualizar(id, aluno);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ALUNO')")
    @Operation(summary = "Remove um aluno pelo id")
    public void deletar(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        validarAcessoProprio(id, usuario);
        service.deletar(id);
    }

    private void validarAcessoProprio(Long id, Usuario usuario) {
        if (usuario.getPerfil() == UsuarioCargo.ALUNO && !usuario.getId().equals(id)) {
            throw new AccessDeniedException("Aluno nao pode acessar dados de outro aluno");
        }
    }
}
