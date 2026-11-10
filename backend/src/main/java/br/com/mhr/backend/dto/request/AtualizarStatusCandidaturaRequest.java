package br.com.mhr.backend.dto.request;

import br.com.mhr.backend.model.Candidatura;
import jakarta.validation.constraints.NotNull;

/** Corpo para o admin mudar o status de uma candidatura (ex.: "Em análise", "Aprovado"). */
public record AtualizarStatusCandidaturaRequest(
        @NotNull Candidatura.StatusCandidatura status
) {
}
