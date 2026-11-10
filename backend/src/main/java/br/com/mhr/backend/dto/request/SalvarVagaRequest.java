package br.com.mhr.backend.dto.request;

import br.com.mhr.backend.model.Vaga;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Corpo de criação/atualização de uma vaga, usado pelo painel admin. */
public record SalvarVagaRequest(
        @NotBlank String titulo,
        @NotBlank String slug,
        @NotBlank String descricao,
        @NotBlank String cidade,
        @NotNull Vaga.ModeloTrabalho modeloTrabalho,
        @NotNull Vaga.StatusVaga status
) {
}
