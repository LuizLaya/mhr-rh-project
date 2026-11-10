package br.com.mhr.backend.controller;

import java.util.UUID;

import br.com.mhr.backend.dto.CandidaturaDto;
import br.com.mhr.backend.dto.request.CriarCandidaturaRequest;
import br.com.mhr.backend.service.CandidaturaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Recebe candidaturas públicas a uma vaga ({@code /api/vagas/{vagaId}/candidaturas}). */
@RestController
@RequestMapping("/api/vagas/{vagaId}/candidaturas")
@RequiredArgsConstructor
public class CandidaturaController {

    private final CandidaturaService service;

    // TODO (Fase 3): aceitar upload direto de arquivo (multipart) e enviar para o
    // Cloudflare R2, em vez de depender de um link que o candidato ja hospedou.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CandidaturaDto candidatar(@PathVariable UUID vagaId, @Valid @RequestBody CriarCandidaturaRequest request) {
        return service.candidatar(vagaId, request);
    }
}
