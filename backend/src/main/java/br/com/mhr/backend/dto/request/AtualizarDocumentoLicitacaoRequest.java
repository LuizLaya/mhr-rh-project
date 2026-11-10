package br.com.mhr.backend.dto.request;

import jakarta.validation.constraints.NotNull;

/** Corpo de {@code PATCH /api/admin/licitacoes/documentos/{id}}. */
public record AtualizarDocumentoLicitacaoRequest(@NotNull Boolean concluido) {
}
