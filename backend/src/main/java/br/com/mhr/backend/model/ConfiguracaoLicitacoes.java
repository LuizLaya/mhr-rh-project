package br.com.mhr.backend.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Linha única (id fixo {@link #ID_UNICO}) com a configuração do sync automático
 * de licitações com o PNCP. Fica no banco, não em {@code application.yml},
 * porque o admin liga/desliga pelo próprio painel, sem precisar de redeploy.
 * Mapeia a tabela {@code configuracao_licitacoes}.
 */
@Entity
@Table(name = "configuracao_licitacoes")
@Getter
@Setter
@NoArgsConstructor
public class ConfiguracaoLicitacoes {

    public static final UUID ID_UNICO = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Id
    private UUID id;

    @Column(name = "busca_pncp_ativa", nullable = false)
    private boolean buscaPncpAtiva;

    // Lista separada por virgula, usada pra combinar com o objetoCompra de cada
    // contratacao trazida do PNCP (ver PncpSyncService.combinaComPalavraChave).
    @Column(name = "palavras_chave", nullable = false, columnDefinition = "text")
    private String palavrasChave;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private Instant atualizadoEm;
}
