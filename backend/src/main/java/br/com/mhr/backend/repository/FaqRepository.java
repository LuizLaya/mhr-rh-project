package br.com.mhr.backend.repository;

import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.model.Faq;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso a dados de {@link Faq}. */
public interface FaqRepository extends JpaRepository<Faq, UUID> {

    List<Faq> findByPublicadoTrueOrderByOrdemExibicaoAsc();
}
