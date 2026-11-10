package br.com.mhr.backend.dto;

import java.time.Instant;
import java.util.UUID;

import br.com.mhr.backend.model.PostBlog;

/** Representação pública de um {@link PostBlog}, exposta pela API. */
public record PostBlogDto(
        UUID id,
        String titulo,
        String slug,
        String resumo,
        String conteudo,
        String urlImagemCapa,
        String categoria,
        boolean publicado,
        Instant publicadoEm
) {
}
