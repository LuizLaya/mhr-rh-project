package br.com.mhr.backend.dto;

/** Estado atual da configuração de busca automática de licitações no PNCP. */
public record ConfiguracaoLicitacoesDto(boolean buscaPncpAtiva, String palavrasChave) {
}
