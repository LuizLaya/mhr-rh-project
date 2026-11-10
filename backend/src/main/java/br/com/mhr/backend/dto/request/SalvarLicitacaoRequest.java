package br.com.mhr.backend.dto.request;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.validation.constraints.NotBlank;

/** Corpo de criação/atualização manual de uma licitação, usado pelo painel admin. */
public record SalvarLicitacaoRequest(
        @NotBlank String orgao,
        @NotBlank String objeto,
        String modalidade,
        String numeroProcesso,
        String uf,
        BigDecimal valorEstimado,
        Instant dataAberturaProposta,
        Instant dataEncerramentoProposta,
        String linkEdital,
        String observacoes
) {
}
