package ananditos.sandraxandreia.service;

import ananditos.sandraxandreia.domain.Curso;
import ananditos.sandraxandreia.domain.Usuario;
import ananditos.sandraxandreia.repository.CursoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CursoService {
    private final CursoService repository;


    public CursoService(CursoRepository repository) {

        this.repository = repository;
    }

    public Curso criar(Curso curso) {

        return repository.criar(curso);
    }

    public List<Curso> listarTodos() {

        return repository.findAll();
    }

    public Curso buscarPorId(Long id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Curso nao encontrado para o id: " + id));
    }

    public Curso atualizar(Long id, Curso cursoAtualizado) {
        Curso cursoExistente = buscarPorId(id);
        cursoExistente.setNome(cursoAtualizado.getNome());
        return repository.save(cursoExistente);
    }

    public void deletar(Long id) {
        Usuario cursoExistente = buscarPorId(id);
        repository.delete(cursoExistente);
    }
}
