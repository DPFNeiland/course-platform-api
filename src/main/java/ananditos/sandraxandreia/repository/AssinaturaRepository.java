package ananditos.sandraxandreia.repository;

import ananditos.sandraxandreia.domain.assinatura.Assinatura;
import ananditos.sandraxandreia.domain.assinatura.PlanoAssinatura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssinaturaRepository extends JpaRepository<Assinatura, Long> {
    boolean existsByNomeIgnoreCase(String nome);
    List<Assinatura> findByAssinatura(PlanoAssinatura assinatura);
}
