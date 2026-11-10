package br.com.mhr.backend.service.pncp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Espelha {@code RecuperarCompraPublicacaoDTO} da API de consulta do PNCP
 * (Portal Nacional de Contratações Públicas, {@code /v1/contratacoes/proposta}),
 * só com os campos que a MHR usa. Datas vêm sem timezone (horário local do
 * Brasil) — convertidas para {@code Instant} em {@link PncpSyncService}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PncpContratacaoDto(
        String numeroControlePNCP,
        String processo,
        String objetoCompra,
        String modalidadeNome,
        BigDecimal valorTotalEstimado,
        LocalDateTime dataAberturaProposta,
        LocalDateTime dataEncerramentoProposta,
        String linkSistemaOrigem,
        String linkProcessoEletronico,
        OrgaoEntidade orgaoEntidade,
        UnidadeOrgao unidadeOrgao
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OrgaoEntidade(String razaoSocial) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UnidadeOrgao(String ufSigla) {
    }
}
