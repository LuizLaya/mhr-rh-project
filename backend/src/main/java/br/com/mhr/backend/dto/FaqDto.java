package br.com.mhr.backend.dto;

import java.time.Instant;
import java.util.UUID;

import br.com.mhr.backend.model.Faq;

/** Representação pública de um {@link Faq}, exposta pela API. */
public record FaqDto(
        UUID id,
        String pergunta,
        String resposta,
        int ordemExibicao,
        boolean publicado,
        Instant atualizadoEm
) {
}
