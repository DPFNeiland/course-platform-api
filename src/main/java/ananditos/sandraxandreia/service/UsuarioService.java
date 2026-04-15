package ananditos.sandraxandreia.service;

import ananditos.sandraxandreia.domain.Usuario;
import ananditos.sandraxandreia.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;

    /**
     * Injeção de dependência por construtor.
     *
     * O Spring localiza o bean UsuarioRepository e injeta automaticamente aqui.
     */
    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public Usuario criar(Usuario usuario) {
        return repository.save(usuario);
    }

    public List<Usuario> listarTodos() {
        return repository.findAll();
    }

    public Usuario buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado para o id: " + id));
    }

    public Usuario atualizar(Long id, Usuario usuarioAtualizado) {
        Usuario usuarioExistente = buscarPorId(id);
        usuarioExistente.setNome(usuarioAtualizado.getNome());
        usuarioExistente.setEmail(usuarioAtualizado.getEmail());
        return repository.save(usuarioExistente);
    }

    public void deletar(Long id) {
        Usuario usuarioExistente = buscarPorId(id);
        repository.delete(usuarioExistente);
    }
}