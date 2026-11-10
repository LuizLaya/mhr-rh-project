package br.com.mhr.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

/** Corpo de criação/atualização de um post do blog, usado pelo painel admin. */
public record SalvarPostBlogRequest(
        @NotBlank String titulo,
        @NotBlank String slug,
        @NotBlank String resumo,
        @NotBlank String conteudo,
        String urlImagemCapa,
        String categoria,
        boolean publicado
) {
}
