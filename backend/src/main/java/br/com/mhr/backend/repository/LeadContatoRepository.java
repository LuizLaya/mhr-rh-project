package br.com.mhr.backend.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.model.LeadContato;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso a dados de {@link LeadContato}. */
public interface LeadContatoRepository extends JpaRepository<LeadContato, UUID> {

    long countByStatus(LeadContato.StatusLead status);

    List<LeadContato> findByCriadoEmAfter(Instant limite);

    List<LeadContato> findByStatusAndCriadoEmBeforeOrderByCriadoEmAsc(LeadContato.StatusLead status, Instant limite);
}
