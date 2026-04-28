package ananditos.sandraxandreia.service;

import ananditos.sandraxandreia.domain.curso.Curso;
import ananditos.sandraxandreia.dto.request.CursoRequestDTO;
import ananditos.sandraxandreia.dto.response.CursoResponseDTO;

import ananditos.sandraxandreia.repository.CursoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CursoService {
    private final CursoRepository cursoRepository;

    public CursoService(CursoRepository cursoRepository) {
        this.cursoRepository = cursoRepository;
    }

    private CursoResponseDTO toResponse(Curso curso) {
        return new CursoResponseDTO(
                curso.getNome(),
                curso.getTipoAssinatura(),
                curso.getTipoCurso()

        );
    }

    public CursoResponseDTO criar(CursoRequestDTO request) {

        var curso = new Curso(
                null,
                request.getNome(),
                request.getTipoAssinatura(),
                request.getTipoCurso()
        );
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
