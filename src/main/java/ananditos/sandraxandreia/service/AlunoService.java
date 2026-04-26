package ananditos.sandraxandreia.service;

import ananditos.sandraxandreia.domain.Aluno;
import ananditos.sandraxandreia.domain.vo.*;
import ananditos.sandraxandreia.dto.AlunoRequestDTO;
import ananditos.sandraxandreia.dto.AlunoResponseDTO;
import ananditos.sandraxandreia.repository.AlunoRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlunoService {
    private final AlunoRepository alunoRepository;
    private final PasswordEncoder passwordEncoder;

    public AlunoService(AlunoRepository alunoRepository1, PasswordEncoder passwordEncoder) {
        this.alunoRepository = alunoRepository1;
        this.passwordEncoder = passwordEncoder;
    }

    private AlunoResponseDTO toResponse(Aluno aluno) {
        return new AlunoResponseDTO(
                aluno.getId(),
                aluno.getNome(),
                aluno.getEmail().getValor(),
                aluno.getCpf().getValor(),
                aluno.getGenero(),
                aluno.getDataNascimento().getData(),
                aluno.getRa().getValor(),
                aluno.getStatus()

        );
    }

    public AlunoResponseDTO criar(AlunoRequestDTO request) {
        String emailNormalizado = request.getEmail() == null ? null : request.getEmail().trim().toLowerCase();
        String cpf = request.getCpf() == null ? null : request.getCpf().trim().toLowerCase();

        if (alunoRepository.existsByEmailValor(emailNormalizado)) {
            throw new RuntimeException("E-mail ja cadastrado");
        }

        if (alunoRepository.existsByCpfValor(cpf)) {
            throw new RuntimeException("CPF ja cadastrado");
        }

        var aluno = new Aluno(
                null,
                request.getNome(),
                request.getEmail(),
                passwordEncoder.encode(request.getSenha()),
                request.getCpf(),
                request.getGenero(),
                request.getDataNascimento(),
                request.getRa(),
                request.getStatus()
        );
        Aluno salvo = alunoRepository.save(aluno);
        return toResponse(salvo);
    }

    public List<AlunoResponseDTO> listarTodos() {
        return alunoRepository.findAll().stream().map(this::toResponse).toList();

    }


    public AlunoResponseDTO buscarPorId(Long id) {
        Aluno aluno = alunoRepository.findById(id).
                orElseThrow(() -> new IllegalArgumentException("`Professor` nao encontrado para o id: " + id));
        return toResponse(aluno);

    }

    public AlunoResponseDTO atualizar(Long id, AlunoRequestDTO request) {
        Aluno aluno = alunoRepository.findById(id).
                orElseThrow(() -> new IllegalArgumentException("`Professor` nao encontrado para o id: " + id));

        aluno.setNome(request.getNome());
        aluno.setEmail(new UsuarioEmail(request.getEmail()));
        aluno.setCpf(new UsuarioCpf(request.getCpf()));
        aluno.setSenha(new UsuarioSenhaCriptografada(request.getSenha()));
        aluno.setGenero(request.getGenero());
        aluno.setDataNascimento(new UsuarioDataNascimento(request.getDataNascimento()));
        aluno.setRa(new AlunoRA(request.getRa()));
        aluno.setStatus(request.getStatus());

        Aluno salvo = alunoRepository.save(aluno);

        return toResponse(salvo);

    }

    public void deletar(Long id) {
        Aluno alunoExistente = alunoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Professor nao encontrado para o id: " + id));

        alunoRepository.delete(alunoExistente);
    }
}
