package ananditos.sandraxandreia.service;

import ananditos.sandraxandreia.domain.curso.Curso;
import ananditos.sandraxandreia.domain.curso.StatusCurso;
import ananditos.sandraxandreia.domain.professor.Professor;
import ananditos.sandraxandreia.dto.request.CursoRequestDTO;
import ananditos.sandraxandreia.dto.response.CursoResponseDTO;
import ananditos.sandraxandreia.exception.RecursoNaoEncontradoException;
import ananditos.sandraxandreia.repository.CursoRepository;
import ananditos.sandraxandreia.repository.ProfessorRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;

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

    public CursoResponseDTO criar(CursoRequestDTO request, Long professorAutenticadoId) {
        validarNomeDuplicado(request.getNome(), null);

        validarProfessorSolicitado(request.getProfessorId(), professorAutenticadoId);

        Professor professor = professorRepository.findById(request.getProfessorId()).
                orElseThrow(() -> new RecursoNaoEncontradoException("Professor nao encontrado"));


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

    public CursoResponseDTO atualizar(Long id, CursoRequestDTO request, Long professorAutenticadoId) {
        Curso curso = buscarCurso(id);
        validarProprietario(curso, professorAutenticadoId);
        validarProfessorSolicitado(request.getProfessorId(), professorAutenticadoId);
        validarNomeDuplicado(request.getNome(), id);
        Professor professor = buscarProfessor(request.getProfessorId());

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

    public void deletar(Long id, Long professorAutenticadoId) {
        Curso curso = buscarCurso(id);
        validarProprietario(curso, professorAutenticadoId);
        cursoRepository.delete(curso);
    }

    private Curso buscarCurso(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Curso nao encontrado para o id: " + id));
    }

    private Professor buscarProfessor(Long id) {
        return professorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Professor nao encontrado para o id: " + id));
    }

    private void validarProfessorSolicitado(Long professorSolicitadoId, Long professorAutenticadoId) {
        if (!Objects.equals(professorSolicitadoId, professorAutenticadoId)) {
            throw new AccessDeniedException("Professor nao pode vincular curso a outro professor");
        }
    }

    private void validarProprietario(Curso curso, Long professorAutenticadoId) {
        if (!Objects.equals(curso.getProfessor().getId(), professorAutenticadoId)) {
            throw new AccessDeniedException("Professor nao pode alterar curso de outro professor");
        }
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
