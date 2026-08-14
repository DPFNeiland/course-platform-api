package ananditos.sandraxandreia.controller;

import ananditos.sandraxandreia.dto.request.CuradorRequestDTO;
import ananditos.sandraxandreia.dto.response.CuradorResponseDTO;
import ananditos.sandraxandreia.service.CuradorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/curador")
@Tag(name = "Curador", description = "API REST de curador")
public class CuradorController {

    private final CuradorService service;

    public CuradorController(CuradorService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("permitAll()")
    @Operation(summary = "Cadastra um novo curador")
    public CuradorResponseDTO criar(@Valid @RequestBody CuradorRequestDTO curador) {
        return service.criar(curador);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CURADOR','ADMIN')")
    @Operation(summary = "Lista todos os curadores")
    public List<CuradorResponseDTO> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CURADOR','ADMIN')")
    @Operation(summary = "Busca um curador pelo id")
    public CuradorResponseDTO buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CURADOR','ADMIN')")
    @Operation(summary = "Atualiza um curador existente")
    public CuradorResponseDTO atualizar(@PathVariable Long id, @Valid @RequestBody CuradorRequestDTO curador) {
        return service.atualizar(id, curador);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('CURADOR','ADMIN')")
    @Operation(summary = "Remove um curador pelo id")
    public void deletar(@PathVariable Long id) {
        service.deletar(id);
    }
}
