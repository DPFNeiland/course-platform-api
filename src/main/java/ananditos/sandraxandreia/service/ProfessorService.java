package ananditos.sandraxandreia.service;

import ananditos.sandraxandreia.domain.professor.Professor;
import ananditos.sandraxandreia.domain.professor.vo.ProfessorAreaFormacao;
import ananditos.sandraxandreia.domain.usuario.vo.UsuarioCpf;
import ananditos.sandraxandreia.domain.usuario.vo.UsuarioDataNascimento;
import ananditos.sandraxandreia.domain.usuario.vo.UsuarioEmail;
import ananditos.sandraxandreia.domain.usuario.vo.UsuarioSenhaCriptografada;
import ananditos.sandraxandreia.dto.request.ProfessorRequestDTO;
import ananditos.sandraxandreia.dto.response.ProfessorResponseDTO;
import ananditos.sandraxandreia.repository.ProfessorRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfessorService {
    private final ProfessorRepository professorRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfessorService(ProfessorRepository professorRepository, PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.professorRepository = professorRepository;
    }
    private ProfessorResponseDTO toResponse(Professor professor) {
        return new ProfessorResponseDTO(
                professor.getId(),
                professor.getNome(),
                professor.getEmail().getValor(),
                professor.getCpf().getValor(),
                professor.getGenero(),
                professor.getDataNascimento().getData(),
                professor.getAreaFormacao().getValor(),
                professor.getHoraAula(),
                professor.getTipoEnsino()

        );
    }

    public ProfessorResponseDTO criar(ProfessorRequestDTO request) {
        String emailNormalizado = request.getEmail() == null ? null : request.getEmail().trim().toLowerCase();
        String cpf = request.getCpf() == null ? null : request.getCpf().trim().toLowerCase();

        if (professorRepository.existsByEmailValor(emailNormalizado)) {
            throw new RuntimeException("E-mail ja cadastrado");
        }

        if (professorRepository.existsByCpfValor(cpf)) {
            throw new RuntimeException("CPF ja cadastrado");
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
                orElseThrow(() -> new IllegalArgumentException("`Professor` nao encontrado para o id: " + id));
        return toResponse(professor);

    }

    public Professor buscarEntidadePorId(Long id) {
        return professorRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Professor nao encontrado para o id: " + id));
    }

    public ProfessorResponseDTO atualizar(Long id, ProfessorRequestDTO request) {
        Professor professor = professorRepository.findById(id).
                orElseThrow(() -> new IllegalArgumentException("`Professor` nao encontrado para o id: " + id));

        professor.setNome(request.getNome());
        professor.setEmail(new UsuarioEmail(request.getEmail()));
        professor.setCpf(new UsuarioCpf(request.getCpf()));
        professor.setSenha(new UsuarioSenhaCriptografada(request.getSenha()));
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
                .orElseThrow(() -> new IllegalArgumentException("Professor nao encontrado para o id: " + id));

        professorRepository.delete(professorExistente);
    }
}
