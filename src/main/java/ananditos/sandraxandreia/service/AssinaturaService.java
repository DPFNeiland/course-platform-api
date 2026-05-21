package ananditos.sandraxandreia.service;

import ananditos.sandraxandreia.domain.assinatura.Assinatura;
import ananditos.sandraxandreia.domain.assinatura.PlanoAssinatura;
import ananditos.sandraxandreia.dto.request.AssinaturaRequestDTO;
import ananditos.sandraxandreia.dto.response.AssinaturaResponseDTO;
import ananditos.sandraxandreia.repository.AssinaturaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssinaturaService {

    private final AssinaturaRepository assinaturaRepository;

    public AssinaturaService(AssinaturaRepository assinaturaRepository) {
        this.assinaturaRepository = assinaturaRepository;
    }

    public AssinaturaResponseDTO criar(AssinaturaRequestDTO request) {
        validarNomeDuplicado(request.getNome(), null);

        Assinatura assinatura = new Assinatura(
                null,
                request.getNome(),
                request.getAssinatura(),
                request.getPreco(),
                List.copyOf(request.getBeneficios())
        );

        return toResponse(assinaturaRepository.save(assinatura));
    }

    public List<AssinaturaResponseDTO> listarTodos() {
        return assinaturaRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AssinaturaResponseDTO> listarPorPlano(PlanoAssinatura plano) {
        return assinaturaRepository.findByAssinatura(plano).stream()
                .map(this::toResponse)
                .toList();
    }

    public AssinaturaResponseDTO buscarPorId(Long id) {
        return toResponse(buscarAssinatura(id));
    }

    public AssinaturaResponseDTO atualizar(Long id, AssinaturaRequestDTO request) {
        validarNomeDuplicado(request.getNome(), id);

        Assinatura assinatura = buscarAssinatura(id);
        assinatura.setNome(request.getNome());
        assinatura.setAssinatura(request.getAssinatura());
        assinatura.setPreco(request.getPreco());
        assinatura.setBeneficios(List.copyOf(request.getBeneficios()));

        return toResponse(assinaturaRepository.save(assinatura));
    }

    public AssinaturaResponseDTO atualizarPlano(Long id, PlanoAssinatura novoPlano) {
        Assinatura assinatura = buscarAssinatura(id);
        assinatura.setAssinatura(novoPlano);
        return toResponse(assinaturaRepository.save(assinatura));
    }

    public void deletar(Long id) {
        assinaturaRepository.delete(buscarAssinatura(id));
    }

    private Assinatura buscarAssinatura(Long id) {
        return assinaturaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assinatura nao encontrada para o id: " + id));
    }

    private AssinaturaResponseDTO toResponse(Assinatura assinatura) {
        return new AssinaturaResponseDTO(
                assinatura.getId(),
                assinatura.getNome(),
                assinatura.getAssinatura(),
                assinatura.getPreco(),
                List.copyOf(assinatura.getBeneficios())
        );
    }

    private void validarNomeDuplicado(String nome, Long idAtual) {
        if (!assinaturaRepository.existsByNomeIgnoreCase(nome)) {
            return;
        }

        if (idAtual == null) {
            throw new RuntimeException("Ja existe uma assinatura com esse nome");
        }

        Assinatura assinaturaAtual = buscarAssinatura(idAtual);
        if (!assinaturaAtual.getNome().equalsIgnoreCase(nome)) {
            throw new RuntimeException("Ja existe uma assinatura com esse nome");
        }
    }
}
