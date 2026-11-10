package br.com.mhr.backend.repository;

import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.model.Depoimento;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso a dados de {@link Depoimento}. */
public interface DepoimentoRepository extends JpaRepository<Depoimento, UUID> {

    List<Depoimento> findByPublicadoTrueOrderByOrdemExibicaoAsc();

    long countByPublicadoTrue();
}
