package ananditos.sandraxandreia.controller;


import ananditos.sandraxandreia.service.CursoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


public class CursoController {
    @RequestMapping("/usuarios")
    @Tag(name = "Usuario", description = "Endpoints didaticos para a entidade Usuario")
    public class UsuarioController {

        private final CursoService service;

        public UsuarioController(Curso service) {
            this.service = service;
        }

        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        @Operation(summary = "Cadastra um novo usuario")
        public Usuario criar(@RequestBody Usuario usuario) {
            return service.criar(usuario);
        }

        @GetMapping
        @Operation(summary = "Lista todos os usuarios")
        public List<Usuario> listarTodos() {
            return service.listarTodos();
        }

        @GetMapping("/{id}")
        @Operation(summary = "Busca um usuario pelo id")
        public Usuario buscarPorId(@PathVariable Long id) {
            return service.buscarPorId(id);
        }

        @PutMapping("/{id}")
        @Operation(summary = "Atualiza um usuario existente")
        public Usuario atualizar(@PathVariable Long id, @RequestBody Usuario usuario) {
            return service.atualizar(id, usuario);
        }

        @DeleteMapping("/{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        @Operation(summary = "Remove um usuario pelo id")
        public void deletar(@PathVariable Long id) {
            service.deletar(id);
        }


}
