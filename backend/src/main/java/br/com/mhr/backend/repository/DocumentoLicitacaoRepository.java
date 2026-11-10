package br.com.mhr.backend.repository;

import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.model.DocumentoLicitacao;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso a dados de {@link DocumentoLicitacao}. */
public interface DocumentoLicitacaoRepository extends JpaRepository<DocumentoLicitacao, UUID> {

    List<DocumentoLicitacao> findByLicitacaoId(UUID licitacaoId);
}
