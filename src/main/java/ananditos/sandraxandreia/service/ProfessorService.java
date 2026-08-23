package ananditos.sandraxandreia.service;

import ananditos.sandraxandreia.domain.professor.Professor;
import ananditos.sandraxandreia.domain.professor.vo.ProfessorAreaFormacao;
import ananditos.sandraxandreia.domain.usuario.vo.UsuarioCpf;
import ananditos.sandraxandreia.domain.usuario.vo.UsuarioDataNascimento;
import ananditos.sandraxandreia.domain.usuario.vo.UsuarioEmail;
import ananditos.sandraxandreia.domain.usuario.vo.UsuarioSenhaCriptografada;
import ananditos.sandraxandreia.dto.request.ProfessorRequestDTO;
import ananditos.sandraxandreia.dto.response.ProfessorResponseDTO;
import ananditos.sandraxandreia.exception.RecursoNaoEncontradoException;
import ananditos.sandraxandreia.exception.ConflitoDadosException;
import ananditos.sandraxandreia.repository.ProfessorRepository;
import ananditos.sandraxandreia.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

import static ananditos.sandraxandreia.service.validation.cpfNormalizado.normalizarCPF;
import static ananditos.sandraxandreia.service.validation.emailNormalizado.normalizarEMAIL;

@Service
public class ProfessorService {
    private final ProfessorRepository professorRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfessorService(ProfessorRepository professorRepository, UsuarioRepository usuarioRepository,
                            PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.professorRepository = professorRepository;
        this.usuarioRepository = usuarioRepository;
    }
    private ProfessorResponseDTO toResponse(Professor professor) {
        return new ProfessorResponseDTO(
                professor.getId(),
                professor.getNome(),
                professor.getEmail().getValor(),
                professor.getCpf().getValor(),
                professor.getGenero(),
                professor.getDataNascimento().getData(),
                professor.getPerfil(),
                professor.getAreaFormacao().getValor(),
                professor.getHoraAula(),
                professor.getTipoEnsino()

                );
    }

    public ProfessorResponseDTO criar(ProfessorRequestDTO request) {
        String emailNormalizado = normalizarEMAIL(request.getEmail());
        String cpf = normalizarCPF(request.getCpf());


        if (usuarioRepository.existsByEmailValor(emailNormalizado)) {
            throw new ConflitoDadosException("E-mail ja cadastrado");
        }

        if (usuarioRepository.existsByCpfValor(cpf)) {
            throw new ConflitoDadosException("CPF ja cadastrado");
        }
        var professor = new Professor(
                null,
                request.getNome(),
                request.getEmail(),
                passwordEncoder.encode(request.getSenha()),
                request.getCpf(),
                request.getGenero(),
                request.getDataNascimento(),
                request.getAreaFormacao(),
                request.getHoraAula(),
                request.getTipoEnsino()
        );
        Professor salvo = professorRepository.save(professor);
        return toResponse(salvo);
    }

    public List<ProfessorResponseDTO> listarTodos() {

        return professorRepository.findAll().stream().map(this::toResponse).toList();
    }


    public ProfessorResponseDTO buscarPorId(Long id) {
        Professor professor = professorRepository.findById(id).
                orElseThrow(() -> new RecursoNaoEncontradoException("Professor nao encontrado para o id: " + id));
        return toResponse(professor);

    }


    public ProfessorResponseDTO atualizar(Long id, ProfessorRequestDTO request) {
        Professor professor = professorRepository.findById(id).
                orElseThrow(() -> new RecursoNaoEncontradoException("Professor nao encontrado para o id: " + id));
        String emailNormalizado = normalizarEMAIL(request.getEmail());
        String cpfNormalizado = normalizarCPF(request.getCpf());
        if (!professor.getEmail().getValor().equals(emailNormalizado)
                && usuarioRepository.existsByEmailValor(emailNormalizado)) {
            throw new ConflitoDadosException("E-mail ja cadastrado");
        }

        if (!professor.getCpf().getValor().equals(cpfNormalizado)
                && usuarioRepository.existsByCpfValor(cpfNormalizado)) {
            throw new ConflitoDadosException("CPF ja cadastrado");
        }
        professor.setNome(request.getNome());
        professor.setEmail(new UsuarioEmail(request.getEmail()));
        professor.setCpf(new UsuarioCpf(request.getCpf()));
        professor.setSenha(new UsuarioSenhaCriptografada(passwordEncoder.encode(request.getSenha())));
        professor.setGenero(request.getGenero());
        professor.setAreaFormacao(new ProfessorAreaFormacao(request.getAreaFormacao()));
        professor.setDataNascimento(new UsuarioDataNascimento(request.getDataNascimento()));
        professor.setAreaFormacao(new ProfessorAreaFormacao(request.getAreaFormacao()));
        professor.setHoraAula(request.getHoraAula());
        professor.setTipoEnsino(request.getTipoEnsino());

        Professor salvo = professorRepository.save(professor);

        return toResponse(salvo);

    }

    public void deletar(Long id) {
        Professor professorExistente = professorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Professor nao encontrado para o id: " + id));

        professorRepository.delete(professorExistente);
    }
}
