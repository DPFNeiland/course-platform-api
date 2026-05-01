package ananditos.sandraxandreia.service;

import ananditos.sandraxandreia.domain.curso.Curso;
import ananditos.sandraxandreia.domain.curso.StatusCurso;
import ananditos.sandraxandreia.domain.professor.Professor;
import ananditos.sandraxandreia.dto.request.CursoRequestDTO;
import ananditos.sandraxandreia.dto.response.CursoResponseDTO;

import ananditos.sandraxandreia.repository.CursoRepository;
import ananditos.sandraxandreia.repository.ProfessorRepository;
import jdk.jshell.Snippet;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CursoService {
    private final CursoRepository cursoRepository;
    private final ProfessorRepository professorRepository;

    public CursoService(CursoRepository cursoRepository, ProfessorRepository professorRepository) {
        this.cursoRepository = cursoRepository;
        this.professorRepository = professorRepository;
    }

    private CursoResponseDTO toResponse(Curso curso) {
        return new CursoResponseDTO(
                curso.getNome(),
                curso.getTipoAssinatura(),
                curso.getTipoCurso(),
                curso.getStatus(),
                curso.getProfessor().getId()

        );
    }

    public CursoResponseDTO criar(CursoRequestDTO request) {

        Professor professor = professorRepository.findById(request.getProfessorIid()).
                orElseThrow(() -> new RuntimeException("Professor não encontrado"));


        var curso = new Curso(
                null,
                request.getNome(),
                request.getTipoAssinatura(),
                request.getTipoCurso()
        );
        curso.setProfessor(professor);

        Curso salvo = cursoRepository.save(curso);
        return toResponse(salvo);
    }

    public List<CursoResponseDTO> listarTodos() {
        return cursoRepository.findAll().stream().map(this::toResponse).toList();

    }


    public CursoResponseDTO buscarPorId(Long id) {
        Curso curso = cursoRepository.findById(id).
                orElseThrow(() -> new IllegalArgumentException("`Curso` nao encontrado para o id: " + id));
        return toResponse(curso);

    }

    public List<CursoResponseDTO> listarPorStatus(StatusCurso status) {
        List<Curso> cursos = cursoRepository.findByStatus(status);

        return cursos.stream()
                .map(this::toResponse)
                .toList();
    }

    public CursoResponseDTO atualizar(Long id, CursoRequestDTO request) {
        Curso curso = cursoRepository.findById(id).
                orElseThrow(() -> new IllegalArgumentException("`Curso` nao encontrado para o id: " + id));

        curso.setNome(request.getNome());
        curso.setTipoAssinatura(request.getTipoAssinatura());
        curso.setTipoCurso(request.getTipoCurso());


        Curso salvo = cursoRepository.save(curso);

        return toResponse(salvo);

    }

    public CursoResponseDTO atualizar_status(Long id, StatusCurso novoStatus) {
        Curso curso = cursoRepository.findById(id).
                orElseThrow(() -> new IllegalArgumentException("`Curso` nao encontrado para o id: " + id));

        curso.setStatus(novoStatus);

        Curso salvo = cursoRepository.save(curso);

        return toResponse(salvo);

    }

    public void deletar(Long id) {
        Curso cursoExistente = cursoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Curso nao encontrado para o id: " + id));

        cursoRepository.delete(cursoExistente);
    }
}
