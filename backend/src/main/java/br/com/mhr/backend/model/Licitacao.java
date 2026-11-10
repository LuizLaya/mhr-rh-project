package br.com.mhr.backend.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

/**
 * Uma licitação pública (pregão, concorrência...) que a MHR identificou e está
 * acompanhando, do achado até o resultado. Mapeia a tabela {@code licitacoes}.
 *
 * <p>{@code origem = PNCP_AUTOMATICO} significa que veio da busca automática no
 * Portal Nacional de Contratações Públicas (ver {@code PncpSyncService}); nesse
 * caso {@code numeroControlePncp} identifica a licitação lá (usado para não
 * importar a mesma duas vezes). Cadastro manual não tem esse número.</p>
 */
@Entity
@Table(name = "licitacoes")
@Getter
@Setter
@NoArgsConstructor
public class Licitacao {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "numero_controle_pncp", unique = true, length = 60)
    private String numeroControlePncp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Origem origem;

    @Column(nullable = false, length = 255)
    private String orgao;

    @Column(nullable = false, columnDefinition = "text")
    private String objeto;

    @Column(length = 100)
    private String modalidade;

    @Column(name = "numero_processo", length = 100)
    private String numeroProcesso;

    @Column(length = 2)
    private String uf;

    @Column(name = "valor_estimado", precision = 14, scale = 2)
    private BigDecimal valorEstimado;

    @Column(name = "data_abertura_proposta")
    private Instant dataAberturaProposta;

    @Column(name = "data_encerramento_proposta")
    private Instant dataEncerramentoProposta;

    @Column(name = "link_edital", length = 500)
    private String linkEdital;

    // Sem @ManyToOne de proposito (mesmo criterio de Depoimento/LeadContato):
    // modulos ficam desacoplados, o front-end resolve o nome cruzando com a
    // lista de usuarios ja carregada.
    @Column(name = "responsavel_id")
    private UUID responsavelId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusLicitacao status = StatusLicitacao.IDENTIFICADA;

    @Column(columnDefinition = "text")
    private String observacoes;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private Instant criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private Instant atualizadoEm;

    public enum Origem {
        PNCP_AUTOMATICO, MANUAL
    }

    public enum StatusLicitacao {
        IDENTIFICADA, EM_ANALISE, DOCUMENTACAO, PROPOSTA_ENVIADA, GANHOU, PERDEU, DESCARTADA
    }
}
