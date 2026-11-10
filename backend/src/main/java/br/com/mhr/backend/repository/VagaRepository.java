package br.com.mhr.backend.repository;

import br.com.mhr.backend.model.Vaga;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso a dados de {@link Vaga}. */
public interface VagaRepository extends JpaRepository<Vaga, UUID> {

    Optional<Vaga> findBySlugAndStatus(String slug, Vaga.StatusVaga status);

    long countByStatus(Vaga.StatusVaga status);
}
