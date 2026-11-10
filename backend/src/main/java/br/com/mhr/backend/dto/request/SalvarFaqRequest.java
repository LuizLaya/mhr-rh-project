package br.com.mhr.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

/** Corpo de criação/atualização de uma pergunta frequente, usado pelo painel admin. */
public record SalvarFaqRequest(
        @NotBlank String pergunta,
        @NotBlank String resposta,
        @PositiveOrZero int ordemExibicao,
        boolean publicado
) {
}
