package br.com.mhr.backend.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.model.Candidatura;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso a dados de {@link Candidatura}. */
public interface CandidaturaRepository extends JpaRepository<Candidatura, UUID> {

    List<Candidatura> findByVagaId(UUID vagaId);

    List<Candidatura> findByCriadoEmAfter(Instant limite);
}
