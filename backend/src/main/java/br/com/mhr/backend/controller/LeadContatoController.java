package br.com.mhr.backend.controller;

import br.com.mhr.backend.dto.LeadContatoDto;
import br.com.mhr.backend.dto.request.CriarLeadContatoRequest;
import br.com.mhr.backend.service.LeadContatoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Recebe o formulário público de contato ({@code /api/leads-contato}). */
@RestController
@RequestMapping("/api/leads-contato")
@RequiredArgsConstructor
public class LeadContatoController {

    private final LeadContatoService service;

    // TODO: disparar e-mail de notificacao imediata pro admin a cada mensagem nova
    // (hoje so ha o alerta diario de mensagens paradas ha mais de 24h, ver
    // NotificacaoLeadsService).
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LeadContatoDto criar(@Valid @RequestBody CriarLeadContatoRequest request) {
        return service.criar(request);
    }
}
