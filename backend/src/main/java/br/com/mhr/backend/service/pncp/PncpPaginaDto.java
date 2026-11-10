package br.com.mhr.backend.service.pncp;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Espelha {@code PaginaRetornoRecuperarCompraPublicacaoDTO} da API de consulta do PNCP. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PncpPaginaDto(List<PncpContratacaoDto> data, long totalPaginas, long numeroPagina) {
}
