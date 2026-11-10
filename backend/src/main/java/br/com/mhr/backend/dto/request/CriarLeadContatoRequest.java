package br.com.mhr.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Corpo do formulário público de contato. Telefone e mensagem são opcionais. */
public record CriarLeadContatoRequest(
        @NotBlank String nome,
        @NotBlank @Email String email,
        String telefone,
        String nomeEmpresa,
        String mensagem,
        @NotBlank String turnstileToken
) {
}
