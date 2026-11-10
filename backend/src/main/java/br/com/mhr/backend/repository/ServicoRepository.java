package br.com.mhr.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.mhr.backend.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso a dados de {@link Servico}. */
public interface ServicoRepository extends JpaRepository<Servico, UUID> {

    List<Servico> findByPublicadoTrueOrderByOrdemExibicaoAsc();

    Optional<Servico> findBySlugAndPublicadoTrue(String slug);

    long countByPublicadoTrue();
}
