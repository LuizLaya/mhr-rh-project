package br.com.mhr.backend.controller.admin;

import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.dto.CandidaturaDto;
import br.com.mhr.backend.dto.request.AtualizarStatusCandidaturaRequest;
import br.com.mhr.backend.service.CandidaturaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Lista e atualiza candidaturas recebidas (painel admin, protegido por JWT). */
@RestController
@RequestMapping("/api/admin/candidaturas")
@RequiredArgsConstructor
public class CandidaturaAdminController {

    private final CandidaturaService service;

    @GetMapping
    public List<CandidaturaDto> listar(@RequestParam(required = false) UUID vagaId) {
        return service.listar(vagaId);
    }

    @PatchMapping("/{id}/status")
    public CandidaturaDto atualizarStatus(@PathVariable UUID id, @Valid @RequestBody AtualizarStatusCandidaturaRequest request) {
        return service.atualizarStatus(id, request);
    }
}
