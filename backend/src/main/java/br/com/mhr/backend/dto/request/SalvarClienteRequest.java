package br.com.mhr.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

/** Corpo de criação/atualização de um cliente, usado pelo painel admin. */
public record SalvarClienteRequest(
        @NotBlank String nomeEmpresa,
        String cnpj,
        String contatoNome,
        String contatoEmail,
        String contatoTelefone,
        String observacoes,
        String logoUrl,
        boolean exibirNoSite
) {
}
