package br.com.mhr.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

/** Corpo de criação/atualização de um integrante da equipe, usado pelo painel admin. */
public record SalvarMembroEquipeRequest(
        @NotBlank String nome,
        String cargo,
        String fotoUrl,
        String bio,
        @PositiveOrZero int ordemExibicao,
        boolean publicado
) {
}
