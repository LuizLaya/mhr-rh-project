package br.com.mhr.backend.dto;

import java.time.Instant;
import java.util.UUID;

import br.com.mhr.backend.model.MembroEquipe;

/** Representação pública de um {@link MembroEquipe}, exposta pela API. */
public record MembroEquipeDto(
        UUID id,
        String nome,
        String cargo,
        String fotoUrl,
        String bio,
        int ordemExibicao,
        boolean publicado,
        Instant atualizadoEm
) {
}
