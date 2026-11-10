package br.com.mhr.backend.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import br.com.mhr.backend.model.Licitacao;

/** Representação de uma {@link Licitacao}, exposta ao painel admin. */
public record LicitacaoDto(
        UUID id,
        String numeroControlePncp,
        Licitacao.Origem origem,
        String orgao,
        String objeto,
        String modalidade,
        String numeroProcesso,
        String uf,
        BigDecimal valorEstimado,
        Instant dataAberturaProposta,
        Instant dataEncerramentoProposta,
        String linkEdital,
        UUID responsavelId,
        Licitacao.StatusLicitacao status,
        String observacoes,
        Instant criadoEm
) {
}
