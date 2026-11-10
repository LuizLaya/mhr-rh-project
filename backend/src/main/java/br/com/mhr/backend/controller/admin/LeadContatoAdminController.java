package br.com.mhr.backend.controller.admin;

import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.dto.LeadContatoDto;
import br.com.mhr.backend.dto.request.AtualizarClienteLeadRequest;
import br.com.mhr.backend.dto.request.AtualizarStatusLeadRequest;
import br.com.mhr.backend.service.LeadContatoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Lista e atualiza o status das mensagens de contato (painel admin, protegido por JWT). */
@RestController
@RequestMapping("/api/admin/leads-contato")
@RequiredArgsConstructor
public class LeadContatoAdminController {

    private final LeadContatoService service;

    @GetMapping
    public List<LeadContatoDto> listarTodos() {
        return service.listarTodos();
    }

    @PatchMapping("/{id}/status")
    public LeadContatoDto atualizarStatus(@PathVariable UUID id, @Valid @RequestBody AtualizarStatusLeadRequest request) {
        return service.atualizarStatus(id, request);
    }

    @PatchMapping("/{id}/cliente")
    public LeadContatoDto atualizarCliente(@PathVariable UUID id, @Valid @RequestBody AtualizarClienteLeadRequest request) {
        return service.atualizarCliente(id, request);
    }
}
