package ananditos.sandraxandreia.service;

import ananditos.sandraxandreia.domain.curso.Curso;
import ananditos.sandraxandreia.domain.professor.Professor;
import ananditos.sandraxandreia.dto.request.CursoRequestDTO;
import ananditos.sandraxandreia.dto.response.CursoResponseDTO;

import ananditos.sandraxandreia.dto.response.ProfessorResponseDTO;
import ananditos.sandraxandreia.repository.CursoRepository;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CursoService {
    private final CursoRepository cursoRepository;
    private final ProfessorService professorService;

    public CursoService(CursoRepository cursoRepository, ProfessorService professorRepository) {
        this.cursoRepository = cursoRepository;
        this.professorService = professorRepository;
    }

    private CursoResponseDTO toResponse(Curso curso) {
        return new CursoResponseDTO(
                curso.getNome(),
                curso.getTipoAssinatura(),
                curso.getTipoCurso(),
                curso.getProfessor().getId()

        );
    }

    public CursoResponseDTO criar(CursoRequestDTO request) {

        Professor professor = professorService.buscarEntidadePorId(request.getProfessorIid());


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

    public CursoResponseDTO atualizar(Long id, CursoRequestDTO request) {
        Curso curso = cursoRepository.findById(id).
                orElseThrow(() -> new IllegalArgumentException("`Curso` nao encontrado para o id: " + id));

        curso.setNome(request.getNome());
        curso.setTipoAssinatura(request.getTipoAssinatura());
        curso.setTipoCurso(request.getTipoCurso());


        Curso salvo = cursoRepository.save(curso);

        return toResponse(salvo);

    }

    public void deletar(Long id) {
        Curso cursoExistente = cursoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Curso nao encontrado para o id: " + id));

        cursoRepository.delete(cursoExistente);
    }
}
