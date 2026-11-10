package br.com.mhr.backend.dto.request;

import java.util.UUID;

/** Corpo de {@code PATCH /api/admin/licitacoes/{id}/responsavel}. {@code responsavelId} nulo remove o responsável. */
public record AtualizarResponsavelLicitacaoRequest(UUID responsavelId) {
}
