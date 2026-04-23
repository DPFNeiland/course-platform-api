package ananditos.sandraxandreia.service;

import ananditos.sandraxandreia.domain.Usuario;
import ananditos.sandraxandreia.domain.vo.UsuarioCpf;
import ananditos.sandraxandreia.domain.vo.UsuarioEmail;
import ananditos.sandraxandreia.domain.vo.UsuarioSenhaCriptografada;
import ananditos.sandraxandreia.dto.UsuarioRequestDTO;
import ananditos.sandraxandreia.dto.UsuarioResponseDTO;
import ananditos.sandraxandreia.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import javax.swing.text.PasswordView;
import java.security.KeyStore;
import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    /**
     * Injeção de dependência por construtor.
     *
     * O Spring localiza o bean UsuarioRepository e injeta automaticamente aqui.
     */
    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    private UsuarioResponseDTO toResponse(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail().getValor(),
                usuario.getCpf().getValor(),
                usuario.getGenero()
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
                passwordEncoder.encode(request.getSenha()) ,
                request.getCpf(),
                request.getGenero()
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
            usuario.setEmail(new UsuarioEmail(request.getEmail()));
            usuario.setCpf(new UsuarioCpf(request.getCpf()));
            usuario.setSenha(new UsuarioSenhaCriptografada(request.getSenha()));
            usuario.setGenero(request.getGenero());
            Usuario salvo = repository.save(usuario);
            return toResponse(salvo);
    }

    public void deletar(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado para o id: " + id));
        repository.delete(usuario);
    }
}