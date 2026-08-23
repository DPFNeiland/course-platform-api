package ananditos.sandraxandreia.service;


import ananditos.sandraxandreia.domain.usuario.Usuario;
import ananditos.sandraxandreia.domain.usuario.vo.UsuarioCpf;
import ananditos.sandraxandreia.domain.usuario.vo.UsuarioDataNascimento;
import ananditos.sandraxandreia.domain.usuario.vo.UsuarioEmail;
import ananditos.sandraxandreia.domain.usuario.vo.UsuarioSenhaCriptografada;
import ananditos.sandraxandreia.dto.request.UsuarioRequestDTO;
import ananditos.sandraxandreia.dto.response.UsuarioResponseDTO;
import ananditos.sandraxandreia.exception.RecursoNaoEncontradoException;
import ananditos.sandraxandreia.exception.ConflitoDadosException;
import ananditos.sandraxandreia.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.List;

import static ananditos.sandraxandreia.service.validation.cpfNormalizado.normalizarCPF;
import static ananditos.sandraxandreia.service.validation.emailNormalizado.normalizarEMAIL;

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
                usuario.getGenero(),
                usuario.getDataNascimento().getData(),
                usuario.getPerfil()
        );
    }



    public UsuarioResponseDTO criar(UsuarioRequestDTO request) {
        String emailNormalizado = normalizarEMAIL(request.getEmail());
        String cpf = normalizarCPF(request.getCpf());

        if (repository.existsByEmailValor(emailNormalizado)) {
            throw new ConflitoDadosException("E-mail ja cadastrado");
        }

        if (repository.existsByCpfValor(cpf)) {
            throw new ConflitoDadosException("CPF ja cadastrado");
        }
        var usuario = new Usuario(
                null,
                request.getNome(),
                request.getEmail(),
                passwordEncoder.encode(request.getSenha()),
                request.getCpf(),
                request.getGenero(),
                request.getDataNascimento(),
                request.getCargo()

        );
        Usuario salvo = repository.save(usuario);
        return toResponse(salvo);

    }

    public List<UsuarioResponseDTO> listarTodos() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public UsuarioResponseDTO buscarPorId(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado para o id: " + id));
        return toResponse(usuario);
    }

        public UsuarioResponseDTO atualizar(Long id, UsuarioRequestDTO request) {
            Usuario usuario = repository.findById(id)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado para o id: " + id));
            if (repository.existsByEmailValor(request.getEmail())) {
                throw new ConflitoDadosException("E-mail ja cadastrado");
            }

            if (repository.existsByCpfValor(request.getCpf())) {
                throw new ConflitoDadosException("CPF ja cadastrado");
            }

            usuario.setNome(request.getNome());
            usuario.setEmail(new UsuarioEmail(request.getEmail()));
            usuario.setCpf(new UsuarioCpf(request.getCpf()));
            usuario.setSenha(new UsuarioSenhaCriptografada(passwordEncoder.encode(request.getSenha())));
            usuario.setGenero(request.getGenero());
            usuario.setDataNascimento(new UsuarioDataNascimento(request.getDataNascimento()));
            usuario.setPerfil(request.getCargo());
            Usuario salvo = repository.save(usuario);
            return toResponse(salvo);
    }

    public void deletar(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado para o id: " + id));
        repository.delete(usuario);
    }
}
