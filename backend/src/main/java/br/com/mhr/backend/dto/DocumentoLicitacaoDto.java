package br.com.mhr.backend.dto;

import java.time.Instant;
import java.util.UUID;

import br.com.mhr.backend.model.DocumentoLicitacao;

/** Representação de um {@link DocumentoLicitacao}, exposta ao painel admin. */
public record DocumentoLicitacaoDto(
        UUID id,
        UUID licitacaoId,
        String nome,
        boolean concluido,
        Instant criadoEm
) {
}
