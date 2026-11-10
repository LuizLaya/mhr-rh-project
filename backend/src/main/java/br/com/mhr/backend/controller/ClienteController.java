package br.com.mhr.backend.controller;

import java.util.List;

import br.com.mhr.backend.dto.ClienteDestaqueDto;
import br.com.mhr.backend.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Clientes em destaque na home do site ({@code /api/clientes-destaque}). */
@RestController
@RequestMapping("/api/clientes-destaque")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService service;

    @GetMapping
    public List<ClienteDestaqueDto> listarDestaques() {
        return service.listarDestaques();
    }
}
