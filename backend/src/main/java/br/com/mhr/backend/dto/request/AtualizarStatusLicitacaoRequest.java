package br.com.mhr.backend.dto.request;

import br.com.mhr.backend.model.Licitacao;
import jakarta.validation.constraints.NotNull;

/** Corpo de {@code PATCH /api/admin/licitacoes/{id}/status}. */
public record AtualizarStatusLicitacaoRequest(@NotNull Licitacao.StatusLicitacao status) {
}
