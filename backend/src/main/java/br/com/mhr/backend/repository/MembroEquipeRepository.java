package br.com.mhr.backend.repository;

import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.model.MembroEquipe;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso a dados de {@link MembroEquipe}. */
public interface MembroEquipeRepository extends JpaRepository<MembroEquipe, UUID> {

    List<MembroEquipe> findByPublicadoTrueOrderByOrdemExibicaoAsc();
}
