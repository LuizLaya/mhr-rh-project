package br.com.mhr.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

/** Corpo de {@code POST /api/admin/licitacoes/{licitacaoId}/documentos}. */
public record SalvarDocumentoLicitacaoRequest(@NotBlank String nome) {
}
