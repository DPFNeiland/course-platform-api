package ananditos.sandraxandreia.controller;

import ananditos.sandraxandreia.domain.assinatura.PlanoAssinatura;
import ananditos.sandraxandreia.dto.request.AssinaturaRequestDTO;
import ananditos.sandraxandreia.dto.response.AssinaturaResponseDTO;
import ananditos.sandraxandreia.service.AssinaturaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/assinatura")
@Tag(name = "Assinatura", description = "API REST de assinatura")
public class AssinaturaController {

    private final AssinaturaService service;

    public AssinaturaController(AssinaturaService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('CURADOR','ADMIN')")
    @Operation(summary = "Cadastra uma nova assinatura")
    public AssinaturaResponseDTO criar(@Valid @RequestBody AssinaturaRequestDTO assinatura) {
        return service.criar(assinatura);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ALUNO','PROFESSOR','CURADOR','ADMIN')")
    @Operation(summary = "Lista as assinaturas, com filtro opcional por plano")
    public List<AssinaturaResponseDTO> listar(@RequestParam(required = false) PlanoAssinatura plano) {
        if (plano != null) {
            return service.listarPorPlano(plano);
        }

        return service.listarTodos();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ALUNO','PROFESSOR','CURADOR','ADMIN')")
    @Operation(summary = "Busca uma assinatura pelo id")
    public AssinaturaResponseDTO buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CURADOR','ADMIN')")
    @Operation(summary = "Atualiza uma assinatura existente")
    public AssinaturaResponseDTO atualizar(@PathVariable Long id, @Valid @RequestBody AssinaturaRequestDTO assinatura) {
        return service.atualizar(id, assinatura);
    }

    @PatchMapping("/{id}/plano")
    @PreAuthorize("hasAnyRole('CURADOR','ADMIN')")
    @Operation(summary = "Atualiza apenas o plano da assinatura")
    public AssinaturaResponseDTO atualizarPlano(@PathVariable Long id, @RequestParam PlanoAssinatura novoPlano) {
        return service.atualizarPlano(id, novoPlano);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('CURADOR','ADMIN')")
    @Operation(summary = "Remove uma assinatura pelo id")
    public void deletar(@PathVariable Long id) {
        service.deletar(id);
    }
}
