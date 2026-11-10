package br.com.mhr.backend.dto;

import java.time.Instant;
import java.util.UUID;

import br.com.mhr.backend.model.LeadContato;

/** Representação pública de um {@link LeadContato}, exposta ao painel admin. */
public record LeadContatoDto(
        UUID id,
        String nome,
        String email,
        String telefone,
        String nomeEmpresa,
        String mensagem,
        LeadContato.StatusLead status,
        Instant concluidoEm,
        boolean virouContrato,
        UUID clienteId,
        Instant criadoEm
) {
}
