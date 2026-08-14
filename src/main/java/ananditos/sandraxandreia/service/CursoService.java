package ananditos.sandraxandreia.service;

import ananditos.sandraxandreia.domain.curso.Curso;
import ananditos.sandraxandreia.domain.curso.StatusCurso;
import ananditos.sandraxandreia.domain.professor.Professor;
import ananditos.sandraxandreia.dto.request.CursoRequestDTO;
import ananditos.sandraxandreia.dto.response.CursoResponseDTO;
import ananditos.sandraxandreia.repository.CursoRepository;
import ananditos.sandraxandreia.repository.ProfessorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

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
                curso.getId(),
                curso.getNome(),
                curso.getTipoAssinatura(),
                curso.getTipoCurso(),
                curso.getStatus(),
                curso.getProfessor().getId()

        );
    }

    public CursoResponseDTO criar(CursoRequestDTO request) {
        validarNomeDuplicado(request.getNome(), null);

        if (cursoRepository.existsByNome(request.getNome())) {
            throw new RuntimeException("Esse nome de curso já existe");
        }

        Professor professor = professorRepository.findById(request.getProfessorId()).
                orElseThrow(() -> new RuntimeException("Professor não encontrado"));


        var curso = new Curso(
                null,
                request.getNome(),
                request.getTipoAssinatura(),
                request.getTipoCurso()
        );
        curso.setProfessor(professor);

        return toResponse(cursoRepository.save(curso));
    }

    public List<CursoResponseDTO> listarTodos() {
        return cursoRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<CursoResponseDTO> listarDisponiveis() {
        return cursoRepository.findByStatusOrderByNomeAsc(StatusCurso.APROVADO).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<CursoResponseDTO> listarPorProfessor(Long professorId) {
        buscarProfessor(professorId);
        return cursoRepository.findByProfessorId(professorId).stream()
                .map(this::toResponse)
                .toList();
    }

    public CursoResponseDTO buscarPorId(Long id) {
        return toResponse(buscarCurso(id));
    }

    public List<CursoResponseDTO> listarPorStatus(StatusCurso status) {
        return cursoRepository.findByStatusOrderByNomeAsc(status).stream()
                .map(this::toResponse)
                .toList();
    }

    public CursoResponseDTO atualizar(Long id, CursoRequestDTO request) {
        Curso curso = buscarCurso(id);
        validarNomeDuplicado(request.getNome(), id);
        Professor professor = buscarProfessor(request.getProfessorId());

        if (cursoRepository.existsByNome(request.getNome())) {
            throw new RuntimeException("Esse nome de curso já existe");
        }
        curso.setNome(request.getNome());
        curso.setTipoAssinatura(request.getTipoAssinatura());
        curso.setTipoCurso(request.getTipoCurso());
        curso.setProfessor(professor);

        return toResponse(cursoRepository.save(curso));
    }

    public CursoResponseDTO atualizar_status(Long id, StatusCurso novoStatus) {
        Curso curso = buscarCurso(id);
        curso.setStatus(novoStatus);
        return toResponse(cursoRepository.save(curso));
    }

    public void deletar(Long id) {
        cursoRepository.delete(buscarCurso(id));
    }

    private Curso buscarCurso(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Curso nao encontrado para o id: " + id));
    }

    private Professor buscarProfessor(Long id) {
        return professorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Professor nao encontrado"));
    }
    private void validarNomeDuplicado(String nome, Long idAtual) {
        if (!cursoRepository.existsByNomeIgnoreCase(nome)) {
            return;
        }

        if (idAtual == null) {
            throw new RuntimeException("Esse nome de curso ja existe");
        }

        Curso cursoAtual = buscarCurso(idAtual);
        if (!Objects.equals(cursoAtual.getNome(), nome)) {
            throw new RuntimeException("Esse nome de curso ja existe");
        }
    }
}
