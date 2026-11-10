package br.com.mhr.backend.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.model.Licitacao;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso a dados de {@link Licitacao}. */
public interface LicitacaoRepository extends JpaRepository<Licitacao, UUID> {

    List<Licitacao> findByStatus(Licitacao.StatusLicitacao status);

    // Teto de seguranca (mesmo padrao do LogSistemaRepository): a tela principal
    // mostra tudo de uma vez (filtra por status no proprio navegador, pros contadores
    // de cada aba baterem sem round-trip extra) - sem um limite, uma conta com muitos
    // anos de uso poderia carregar um numero de registros que degrada a tela.
    List<Licitacao> findTop500ByOrderByCriadoEmDesc();

    boolean existsByNumeroControlePncp(String numeroControlePncp);

    List<Licitacao> findByDataEncerramentoPropostaBeforeAndStatusNotIn(
            Instant limite, List<Licitacao.StatusLicitacao> statusExcluidos);
}
