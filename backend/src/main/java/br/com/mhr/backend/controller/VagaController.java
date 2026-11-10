package br.com.mhr.backend.controller;

import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.dto.VagaDto;
import br.com.mhr.backend.service.VagaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints públicos de vagas ({@code /api/vagas}).
 *
 * <p>Fatia de referência (model → repository → dto → mapper → service → controller)
 * para replicar o mesmo padrão em outros módulos.
 */
@RestController
@RequestMapping("/api/vagas")
@RequiredArgsConstructor
public class VagaController {

    private final VagaService service;

    /** Lista as vagas com status {@code ABERTA}. */
    @GetMapping
    public List<VagaDto> listarVagasAbertas() {
        return service.listarAbertas();
    }

    /** Busca uma vaga pelo id, independente do status. */
    @GetMapping("/{id}")
    public VagaDto buscarPorId(@PathVariable UUID id) {
        return service.buscarPorId(id);
    }
}
