package br.com.mhr.backend.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

/** Corpo de criação/atualização de um depoimento, usado pelo painel admin. */
public record SalvarDepoimentoRequest(
        @NotBlank String nomeAutor,
        String cargoAutor,
        String nomeEmpresa,
        @NotBlank String conteudo,
        String urlFoto,
        @PositiveOrZero int ordemExibicao,
        boolean publicado,
        UUID clienteId
) {
}
