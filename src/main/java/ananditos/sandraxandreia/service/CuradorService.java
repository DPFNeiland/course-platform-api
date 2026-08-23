package ananditos.sandraxandreia.service;

import ananditos.sandraxandreia.domain.curador.Curador;
import ananditos.sandraxandreia.domain.usuario.vo.UsuarioCpf;
import ananditos.sandraxandreia.domain.usuario.vo.UsuarioDataNascimento;
import ananditos.sandraxandreia.domain.usuario.vo.UsuarioEmail;
import ananditos.sandraxandreia.domain.usuario.vo.UsuarioSenhaCriptografada;
import ananditos.sandraxandreia.dto.request.CuradorRequestDTO;
import ananditos.sandraxandreia.dto.response.CuradorResponseDTO;
import ananditos.sandraxandreia.exception.RecursoNaoEncontradoException;
import ananditos.sandraxandreia.exception.ConflitoDadosException;
import ananditos.sandraxandreia.repository.CuradorRepository;
import ananditos.sandraxandreia.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

import static ananditos.sandraxandreia.service.validation.cpfNormalizado.normalizarCPF;
import static ananditos.sandraxandreia.service.validation.emailNormalizado.normalizarEMAIL;

@Service
public class CuradorService {

    private final CuradorRepository curadorRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public CuradorService(CuradorRepository curadorRepository, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.curadorRepository = curadorRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public CuradorResponseDTO criar(CuradorRequestDTO request) {
        validarDuplicidade(null, request.getEmail(), request.getCpf());

        Curador curador = new Curador(
                null,
                request.getNome(),
                request.getEmail(),
                passwordEncoder.encode(request.getSenha()),
                request.getCpf(),
                request.getGenero(),
                request.getDataNascimento()
        );

        return toResponse(curadorRepository.save(curador));
    }

    public List<CuradorResponseDTO> listarTodos() {
        return curadorRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public CuradorResponseDTO buscarPorId(Long id) {
        return toResponse(buscarCurador(id));
    }

    public CuradorResponseDTO atualizar(Long id, CuradorRequestDTO request) {
        Curador curador = buscarCurador(id);
        validarDuplicidade(curador, request.getEmail(), request.getCpf());

        curador.setNome(request.getNome());
        curador.setEmail(new UsuarioEmail(request.getEmail()));
        curador.setCpf(new UsuarioCpf(request.getCpf()));
        curador.setSenha(new UsuarioSenhaCriptografada(passwordEncoder.encode(request.getSenha())));
        curador.setGenero(request.getGenero());
        curador.setDataNascimento(new UsuarioDataNascimento(request.getDataNascimento()));

        return toResponse(curadorRepository.save(curador));
    }

    public void deletar(Long id) {
        curadorRepository.delete(buscarCurador(id));
    }

    private Curador buscarCurador(Long id) {
        return curadorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Curador nao encontrado para o id: " + id));
    }

    private CuradorResponseDTO toResponse(Curador curador) {
        return new CuradorResponseDTO(
                curador.getId(),
                curador.getNome(),
                curador.getEmail().getValor(),
                curador.getCpf().getValor(),
                curador.getGenero(),
                curador.getDataNascimento().getData(),
                curador.getPerfil()
        );
    }

    private void validarDuplicidade(Curador curadorAtual, String email, String cpf) {
        String emailNormalizado = normalizarEMAIL(email);
        String cpfNormalizado = normalizarCPF(cpf);

        if (curadorAtual == null || !curadorAtual.getEmail().getValor().equals(emailNormalizado)) {
            if (usuarioRepository.existsByEmailValor(emailNormalizado)) {
                throw new ConflitoDadosException("E-mail ja cadastrado");
            }
        }

        if (curadorAtual == null || !curadorAtual.getCpf().getValor().equals(cpfNormalizado)) {
            if (usuarioRepository.existsByCpfValor(cpfNormalizado)) {
                throw new ConflitoDadosException("CPF ja cadastrado");
            }
        }
    }
}
