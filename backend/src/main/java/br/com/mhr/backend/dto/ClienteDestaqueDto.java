package br.com.mhr.backend.dto;

/**
 * Projeção pública e mínima de um {@link br.com.mhr.backend.model.Cliente} para a
 * home do site - nunca expõe CNPJ, contato ou observações, só o que o admin marcou
 * para aparecer (ver {@code exibirNoSite}).
 */
public record ClienteDestaqueDto(String nomeEmpresa, String logoUrl) {
}
