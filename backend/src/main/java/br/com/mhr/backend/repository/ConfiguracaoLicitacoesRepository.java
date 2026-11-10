package br.com.mhr.backend.repository;

import java.util.UUID;

import br.com.mhr.backend.model.ConfiguracaoLicitacoes;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso a dados de {@link ConfiguracaoLicitacoes} (sempre uma única linha). */
public interface ConfiguracaoLicitacoesRepository extends JpaRepository<ConfiguracaoLicitacoes, UUID> {
}
