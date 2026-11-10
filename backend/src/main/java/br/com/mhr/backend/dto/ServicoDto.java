package br.com.mhr.backend.dto;

import java.time.Instant;
import java.util.UUID;

import br.com.mhr.backend.model.Servico;

/** Representação pública de um {@link Servico}, exposta pela API. */
public record ServicoDto(
        UUID id,
        String titulo,
        String slug,
        String resumo,
        String conteudo,
        String icone,
        int ordemExibicao,
        boolean publicado,
        Instant atualizadoEm
) {
}
