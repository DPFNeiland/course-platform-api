package ananditos.sandraxandreia.service;

import ananditos.sandraxandreia.domain.aluno.Aluno;
import ananditos.sandraxandreia.domain.curso.Curso;
import ananditos.sandraxandreia.domain.matricula.Matricula;
import ananditos.sandraxandreia.dto.request.MatriculaRequestDTO;
import ananditos.sandraxandreia.dto.response.MatriculaResponseDTO;
import ananditos.sandraxandreia.repository.AlunoRepository;
import ananditos.sandraxandreia.repository.CursoRepository;
import ananditos.sandraxandreia.repository.MatriculaRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class MatriculaService {

    private final MatriculaRepository matriculaRepository;
    private final CursoRepository cursoRepository;
    private final AlunoRepository alunoRepository;

    public MatriculaService(MatriculaRepository matriculaRepository, CursoRepository cursoRepository, AlunoRepository alunoRepository) {
        this.matriculaRepository = matriculaRepository;
        this.cursoRepository = cursoRepository;
        this.alunoRepository = alunoRepository;
    }

    private MatriculaResponseDTO toResponse(Matricula matricula) {
        return new MatriculaResponseDTO(
                matricula.getDataMatricula().getData(),
                matricula.getStatus(),
                matricula.getAluno().getId(),
                matricula.getCurso().getId()

        );
    }

    public MatriculaResponseDTO criar(MatriculaRequestDTO request) {
        System.out.println("AlunoId: " + request.getAlunoId());
        Aluno aluno = alunoRepository.findById(request.getAlunoId()).
                orElseThrow(() -> new RuntimeException("Aluno não encontrado"));

        Curso curso = cursoRepository.findById(request.getCursoId()).
                orElseThrow(() -> new RuntimeException("Curso não encontrado"));


        var matricula = new Matricula(
                null,
                request.getStatus()
        );
        matricula.setAluno(aluno);
        matricula.setCurso(curso);

        Matricula salvo = matriculaRepository.save(matricula);
        return toResponse(salvo);
    }

    public List<MatriculaResponseDTO> listarTodos() {
        return matriculaRepository.findAll().stream().map(this::toResponse).toList();

    }


    public MatriculaResponseDTO buscarPorId(Long id) {
        Matricula matricula = matriculaRepository.findById(id).
                orElseThrow(() -> new IllegalArgumentException("`Matricula` nao encontrado para o id: " + id));
        return toResponse(matricula);

    }

    public MatriculaResponseDTO atualizar(Long id, MatriculaRequestDTO request) {
        Matricula matricula = matriculaRepository.findById(id).
                orElseThrow(() -> new IllegalArgumentException("`Matricula` nao encontrado para o id: " + id));

        matricula.setStatus(request.getStatus());

        Matricula salvo = matriculaRepository.save(matricula);

        return toResponse(salvo);

    }

    public void deletar(Long id) {
        Matricula matriculaExistente = matriculaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Matricula nao encontrado para o id: " + id));

        matriculaRepository.delete(matriculaExistente);
    }
}
