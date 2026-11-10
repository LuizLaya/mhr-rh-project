package br.com.mhr.backend.dto;

import java.time.Instant;
import java.util.UUID;

import br.com.mhr.backend.model.LogSistema;

/** Representação de um {@link LogSistema}, exposta ao painel admin. */
public record LogSistemaDto(
        UUID id,
        LogSistema.Nivel nivel,
        String categoria,
        String mensagem,
        String usuarioEmail,
        String origem,
        String endpoint,
        String correlacaoId,
        Instant criadoEm
) {
}
