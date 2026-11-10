package br.com.mhr.backend.dto;

import java.time.Instant;
import java.util.UUID;

import br.com.mhr.backend.model.Vaga;

/** Representação pública de uma {@link Vaga}, exposta pela API. */
public record VagaDto(
        UUID id,
        String titulo,
        String slug,
        String descricao,
        String cidade,
        Vaga.ModeloTrabalho modeloTrabalho,
        Vaga.StatusVaga status,
        Instant publicadoEm
) {
}
