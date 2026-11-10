package br.com.mhr.backend.dto;

import java.time.Instant;
import java.util.UUID;

import br.com.mhr.backend.model.Candidatura;

/** Representação pública de uma {@link Candidatura}, exposta ao painel admin. */
public record CandidaturaDto(
        UUID id,
        UUID vagaId,
        String nomeCandidato,
        String emailCandidato,
        String telefoneCandidato,
        String urlCurriculo,
        boolean consentimentoLgpd,
        Candidatura.StatusCandidatura status,
        Instant criadoEm
) {
}
