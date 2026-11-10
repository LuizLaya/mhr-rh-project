package br.com.mhr.backend.dto;

import java.util.UUID;

import br.com.mhr.backend.model.Depoimento;

/** Representação pública de um {@link Depoimento}, exposta pela API. */
public record DepoimentoDto(
        UUID id,
        String nomeAutor,
        String cargoAutor,
        String nomeEmpresa,
        String conteudo,
        String urlFoto,
        int ordemExibicao,
        boolean publicado,
        UUID clienteId
) {
}
