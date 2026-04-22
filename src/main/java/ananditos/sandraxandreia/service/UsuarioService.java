package ananditos.sandraxandreia.service;

import ananditos.sandraxandreia.domain.Usuario;
import ananditos.sandraxandreia.domain.vo.SenhaCriptografada;
import ananditos.sandraxandreia.dto.UsuarioRequestDTO;
import ananditos.sandraxandreia.dto.UsuarioResponseDTO;
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

    private UsuarioResponseDTO toResponse(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail()
        );
    }


    public UsuarioResponseDTO criar(UsuarioRequestDTO request) {
        var usuario = new Usuario(
                null,
                request.getNome(),
                request.getEmail(),
                request.getSenha()
        );
        Usuario salvo = repository.save(usuario);
        return toResponse(salvo);

    }

    public List<UsuarioResponseDTO> listarTodos() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public UsuarioResponseDTO buscarPorId(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado para o id: " + id));
        return toResponse(usuario);
    }

        public UsuarioResponseDTO atualizar(Long id, UsuarioRequestDTO request) {
            Usuario usuario = repository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado para o id: " + id));
            usuario.setNome(request.getNome());
            usuario.setEmail(request.getEmail());
            usuario.setSenha(new SenhaCriptografada(request.getSenha()));
            Usuario salvo = repository.save(usuario);
            return toResponse(salvo);
    }

    public void deletar(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado para o id: " + id));
        repository.delete(usuario);
    }
}