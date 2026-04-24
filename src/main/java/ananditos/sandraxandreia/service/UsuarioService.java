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

<<<<<<< Updated upstream
    public Usuario criar(Usuario usuario) {
        return repository.save(usuario);
    }

    public List<Usuario> listarTodos() {
        return repository.findAll();
=======
    private UsuarioResponseDTO toResponse(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail().getValor(),
                usuario.getCpf().getValor(),
                usuario.getGenero(),
                usuario.getDataNascimento().getData()
        );
    }


    public UsuarioResponseDTO criar(UsuarioRequestDTO request) {

        String emailNormalizado = request.getEmail() == null ? null : request.getEmail().trim().toLowerCase();
        String cpf = request.getCpf() == null ? null : request.getCpf().trim().toLowerCase();

        if (repository.existsByEmailValor(emailNormalizado)) {
            throw new RuntimeException("E-mail ja cadastrado");
        }

        if (repository.existsByCpfValor(cpf)) {
            throw new RuntimeException("CPF ja cadastrado");
        }

        var usuario = new Usuario(
                null,
                request.getNome(),
                request.getEmail(),
                request.getCpf(),
                passwordEncoder.encode(request.getSenha()),
                request.getDataNascimento(),
                request.getGenero()
        );
        Usuario salvo = repository.save(usuario);
        return toResponse(salvo);

>>>>>>> Stashed changes
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