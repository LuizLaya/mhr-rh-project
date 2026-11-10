package br.com.mhr.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Corpo de {@code PATCH /api/admin/licitacoes/configuracao} (liga/desliga a busca automática no PNCP e ajusta as palavras-chave). */
public record AtualizarConfiguracaoLicitacoesRequest(@NotNull Boolean buscaPncpAtiva, @NotBlank String palavrasChave) {
}
