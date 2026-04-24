package ananditos.sandraxandreia.controller;

import ananditos.sandraxandreia.dto.UsuarioRequestDTO;
import ananditos.sandraxandreia.dto.UsuarioResponseDTO;
import ananditos.sandraxandreia.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Camada Controller.
 *
 * Recebe requisicoes HTTP, conversa com a camada de servico
 * e devolve respostas em JSON.
 */
@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuario", description = "Endpoints didaticos para a entidade Usuario")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um novo usuario")
    public UsuarioResponseDTO criar(@RequestBody UsuarioRequestDTO usuario) {
        return service.criar(usuario);
    }

    @GetMapping
    @Operation(summary = "Lista todos os usuarios")
    public List<UsuarioResponseDTO> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um usuario pelo id")
    public UsuarioResponseDTO buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um usuario existente")
    public UsuarioResponseDTO atualizar(@PathVariable Long id, @RequestBody UsuarioRequestDTO usuario) {
        return service.atualizar(id, usuario);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove um usuario pelo id")
    public void deletar(@PathVariable Long id) {

        service.deletar(id);
    }
}
