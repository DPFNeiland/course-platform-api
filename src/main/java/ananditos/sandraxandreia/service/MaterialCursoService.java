package ananditos.sandraxandreia.service;

import ananditos.sandraxandreia.domain.curso.Curso;
import ananditos.sandraxandreia.domain.curso.MaterialCurso;
import ananditos.sandraxandreia.domain.curso.TipoMaterialCurso;
import ananditos.sandraxandreia.domain.usuario.Usuario;
import ananditos.sandraxandreia.domain.usuario.UsuarioCargo;
import ananditos.sandraxandreia.dto.request.CursoMaterialLinkRequestDTO;
import ananditos.sandraxandreia.dto.response.CursoMaterialResponseDTO;
import ananditos.sandraxandreia.exception.RecursoNaoEncontradoException;
import ananditos.sandraxandreia.repository.CursoRepository;
import ananditos.sandraxandreia.repository.MaterialCursoRepository;
import ananditos.sandraxandreia.repository.MatriculaRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static ananditos.sandraxandreia.service.validation.emailNormalizado.normalizarEMAIL;

@Service
public class MaterialCursoService {

    private final MaterialCursoRepository materialCursoRepository;
    private final CursoRepository cursoRepository;
    private final MatriculaRepository matriculaRepository;

    public MaterialCursoService(MaterialCursoRepository materialCursoRepository, CursoRepository cursoRepository,
                                MatriculaRepository matriculaRepository) {
        this.materialCursoRepository = materialCursoRepository;
        this.cursoRepository = cursoRepository;
        this.matriculaRepository = matriculaRepository;
    }

    public CursoMaterialResponseDTO adicionarLink(Long cursoId, CursoMaterialLinkRequestDTO request, String emailProfessorAutenticado) {
        Curso curso = buscarCurso(cursoId);
        validarProfessorResponsavel(curso, emailProfessorAutenticado);

        MaterialCurso material = new MaterialCurso(null, curso, normalizarTitulo(request.getTitulo()), TipoMaterialCurso.LINK);
        material.setUrl(request.getUrl().trim());

        return toResponse(materialCursoRepository.save(material));
    }

    public CursoMaterialResponseDTO adicionarArquivo(Long cursoId, String titulo, MultipartFile arquivo, String emailProfessorAutenticado) {
        Curso curso = buscarCurso(cursoId);
        validarProfessorResponsavel(curso, emailProfessorAutenticado);

        if (arquivo == null || arquivo.isEmpty()) {
            throw new RuntimeException("Arquivo do material obrigatorio");
        }

        MaterialCurso material = new MaterialCurso(null, curso, normalizarTitulo(titulo), TipoMaterialCurso.ARQUIVO);
        material.setNomeArquivo(arquivo.getOriginalFilename());
        material.setContentType(arquivo.getContentType());

        try {
            material.setDadosArquivo(arquivo.getBytes());
        } catch (IOException ex) {
            throw new RuntimeException("Nao foi possivel ler o arquivo enviado");
        }

        return toResponse(materialCursoRepository.save(material));
    }

    public List<CursoMaterialResponseDTO> listarPorCurso(Long cursoId, Usuario usuarioAutenticado) {
        validarAcessoDeLeitura(cursoId, usuarioAutenticado);
        buscarCurso(cursoId);
        return materialCursoRepository.findByCursoIdOrderByDataCadastroAsc(cursoId).stream()
                .map(this::toResponse)
                .toList();
    }

    public MaterialCurso buscarArquivo(Long cursoId, Long materialId, Usuario usuarioAutenticado) {
        validarAcessoDeLeitura(cursoId, usuarioAutenticado);
        MaterialCurso material = materialCursoRepository.findByIdAndCurso_Id(materialId, cursoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Material nao encontrado para o curso informado"));

        if (material.getTipo() != TipoMaterialCurso.ARQUIVO) {
            throw new IllegalArgumentException("Material informado nao eh um arquivo");
        }

        return material;
    }

    private void validarAcessoDeLeitura(Long cursoId, Usuario usuarioAutenticado) {
        if (usuarioAutenticado.getPerfil() == UsuarioCargo.ALUNO
                && !matriculaRepository.existsByAluno_IdAndCurso_Id(usuarioAutenticado.getId(), cursoId)) {
            throw new AccessDeniedException("Aluno nao possui matricula neste curso");
        }
    }

    private Curso buscarCurso(Long cursoId) {
        return cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Curso nao encontrado para o id: " + cursoId));
    }

    private void validarProfessorResponsavel(Curso curso, String emailProfessorAutenticado) {
        String emailNormalizado = normalizarEMAIL(emailProfessorAutenticado);
        String emailProfessorCurso = curso.getProfessor().getEmail().getValor();

        if (!emailProfessorCurso.equals(emailNormalizado)) {
            throw new AccessDeniedException("Professor nao pode alterar materiais de curso que nao pertence a ele");
        }
    }

    private String normalizarTitulo(String titulo) {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new RuntimeException("Titulo do material obrigatorio");
        }

        return titulo.trim();
    }

    private CursoMaterialResponseDTO toResponse(MaterialCurso material) {
        return new CursoMaterialResponseDTO(
                material.getId(),
                material.getCurso().getId(),
                material.getTitulo(),
                material.getTipo(),
                material.getUrl(),
                material.getNomeArquivo(),
                material.getContentType(),
                material.getDataCadastro()
        );
    }
}
