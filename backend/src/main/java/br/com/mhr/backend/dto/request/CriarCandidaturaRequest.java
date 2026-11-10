package br.com.mhr.backend.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Corpo da candidatura pública a uma vaga. {@code urlCurriculo} por enquanto é um link
 * já hospedado pelo candidato; upload direto de arquivo (Cloudflare R2) fica para a Fase 3.
 */
public record CriarCandidaturaRequest(
        @NotBlank String nomeCandidato,
        @NotBlank @Email String emailCandidato,
        String telefoneCandidato,
        @NotBlank String urlCurriculo,
        @AssertTrue(message = "E preciso aceitar o uso dos dados pessoais (LGPD)") boolean consentimentoLgpd,
        @NotBlank String turnstileToken
) {
}
