package br.com.mhr.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

/** Corpo de criação/atualização de um serviço, usado pelo painel admin. */
public record SalvarServicoRequest(
        @NotBlank String titulo,
        @NotBlank String slug,
        @NotBlank String resumo,
        @NotBlank String conteudo,
        String icone,
        @PositiveOrZero int ordemExibicao,
        boolean publicado
) {
}
