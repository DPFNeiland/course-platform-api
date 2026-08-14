package ananditos.sandraxandreia.service;

import ananditos.sandraxandreia.domain.aluno.Aluno;
import ananditos.sandraxandreia.domain.curso.Curso;
import ananditos.sandraxandreia.domain.curso.StatusCurso;
import ananditos.sandraxandreia.domain.matricula.Matricula;
import ananditos.sandraxandreia.domain.matricula.StatusMatricula;
import ananditos.sandraxandreia.dto.request.MatriculaRequestDTO;
import ananditos.sandraxandreia.dto.response.MatriculaResponseDTO;
import ananditos.sandraxandreia.repository.AlunoRepository;
import ananditos.sandraxandreia.repository.CursoRepository;
import ananditos.sandraxandreia.repository.MatriculaRepository;
import org.springframework.dao.DataIntegrityViolationException;
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
                matricula.getId(),
                matricula.getDataMatricula().getData(),
                matricula.getStatus(),
                matricula.getAluno().getId(),
                matricula.getCurso().getId()

        );
    }

    public MatriculaResponseDTO criar(MatriculaRequestDTO request) {
        Aluno aluno = buscarAluno(request.getAlunoId());
        Curso curso = buscarCurso(request.getCursoId());

        validarCursoDisponivelParaMatricula(curso);
        validarMatriculaDuplicada(request.getAlunoId(), request.getCursoId(), null);

        Matricula matricula = new Matricula(
                null,
                request.getStatus()
        );
        matricula.setAluno(aluno);
        matricula.setCurso(curso);

        return salvarMatricula(matricula);
    }

    public List<MatriculaResponseDTO> listarTodos() {
        return matriculaRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<MatriculaResponseDTO> listarPorAluno(Long alunoId) {
        buscarAluno(alunoId);
        return matriculaRepository.findByAluno_Id(alunoId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<MatriculaResponseDTO> listarPorCurso(Long cursoId) {
        buscarCurso(cursoId);
        return matriculaRepository.findByCurso_Id(cursoId).stream()
                .map(this::toResponse)
                .toList();
    }

    public MatriculaResponseDTO buscarPorId(Long id) {
        return toResponse(buscarMatricula(id));
    }

    public MatriculaResponseDTO atualizar(Long id, MatriculaRequestDTO request) {
        Matricula matricula = buscarMatricula(id);
        Aluno aluno = buscarAluno(request.getAlunoId());
        Curso curso = buscarCurso(request.getCursoId());

        validarCursoDisponivelParaMatricula(curso);
        validarMatriculaDuplicada(request.getAlunoId(), request.getCursoId(), id);

        matricula.setStatus(request.getStatus());
        matricula.setAluno(aluno);
        matricula.setCurso(curso);

        return salvarMatricula(matricula);
    }

    public MatriculaResponseDTO atualizarStatus(Long id, StatusMatricula novoStatus) {
        Matricula matricula = buscarMatricula(id);
        matricula.setStatus(novoStatus);
        return salvarMatricula(matricula);
    }

    public void deletar(Long id) {
        matriculaRepository.delete(buscarMatricula(id));
    }

    private Matricula buscarMatricula(Long id) {
        return matriculaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Matricula nao encontrada para o id: " + id));
    }

    private Aluno buscarAluno(Long id) {
        return alunoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aluno nao encontrado"));
    }

    private Curso buscarCurso(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Curso nao encontrado"));
    }

    private void validarCursoDisponivelParaMatricula(Curso curso) {
        if (curso.getStatus() != StatusCurso.APROVADO) {
            throw new RuntimeException("Somente cursos aprovados podem receber matriculas");
        }
    }

    private void validarMatriculaDuplicada(Long alunoId, Long cursoId, Long matriculaAtualId) {
        if (!matriculaRepository.existsByAluno_IdAndCurso_Id(alunoId, cursoId)) {
            return;
        }

        if (matriculaAtualId == null) {
            throw new RuntimeException("Aluno ja matriculado nesse curso");
        }

        Matricula matriculaAtual = buscarMatricula(matriculaAtualId);
        boolean mesmaMatricula = matriculaAtual.getAluno().getId().equals(alunoId)
                && matriculaAtual.getCurso().getId().equals(cursoId);

        if (!mesmaMatricula) {
            throw new RuntimeException("Aluno ja matriculado nesse curso");
        }
    }

    private MatriculaResponseDTO salvarMatricula(Matricula matricula) {
        try {
            return toResponse(matriculaRepository.saveAndFlush(matricula));
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException("Aluno ja matriculado nesse curso");
        }
    }
}
