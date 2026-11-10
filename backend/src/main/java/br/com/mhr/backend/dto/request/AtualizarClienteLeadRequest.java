package br.com.mhr.backend.dto.request;

import java.util.UUID;

import br.com.mhr.backend.service.LeadContatoService;
import jakarta.validation.constraints.NotNull;

/**
 * Corpo de {@code PATCH /api/admin/leads-contato/{id}/cliente}. Quando
 * {@code virouContrato} é {@code true}, {@code clienteId} é obrigatório (ver
 * {@link LeadContatoService#atualizarCliente}); quando é {@code false}, o
 * vínculo é sempre removido, mesmo que {@code clienteId} venha preenchido.
 */
public record AtualizarClienteLeadRequest(@NotNull Boolean virouContrato, UUID clienteId) {
}
