package br.com.mhr.backend.dto;

import java.time.Instant;
import java.util.UUID;

import br.com.mhr.backend.model.Cliente;

/** Representação de um {@link Cliente}, exposta ao painel admin. */
public record ClienteDto(
        UUID id,
        String nomeEmpresa,
        String cnpj,
        String contatoNome,
        String contatoEmail,
        String contatoTelefone,
        String observacoes,
        String logoUrl,
        boolean exibirNoSite,
        Instant criadoEm
) {
}
