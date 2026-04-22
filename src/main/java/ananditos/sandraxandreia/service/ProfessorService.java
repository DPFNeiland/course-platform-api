package ananditos.sandraxandreia.service;

import ananditos.sandraxandreia.domain.Professor;
import ananditos.sandraxandreia.repository.ProfessorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfessorService {
    private final ProfessorRepository professorRepository;

    public ProfessorService(ProfessorRepository professorRepository) {
        this.professorRepository = professorRepository;
    }


    public Professor criar(Professor professor) {

        return professorRepository.save(professor);
    }

    public List<Professor> listarTodos() {
        return professorRepository.findAll();
    }


    public Professor buscarPorId(Long id) {
        return professorRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("`Professor` nao encontrado para o id: " + id));
    }

    public Professor atualizar(Long id, Professor professorAtualizado) {
        Professor professorExistente = buscarPorId(id);
        professorExistente.setNome(professorAtualizado.getNome());
        return professorRepository.save(professorExistente);
    }

    public void deletar(Long id) {
        Professor professorExistente = buscarPorId(id);
        professorRepository.delete(professorExistente);
    }
}
