package br.com.mhr.backend.dto.request;

import br.com.mhr.backend.model.LeadContato;
import jakarta.validation.constraints.NotNull;

/** Corpo de {@code PATCH /api/admin/leads-contato/{id}/status}. */
public record AtualizarStatusLeadRequest(@NotNull LeadContato.StatusLead status) {
}
