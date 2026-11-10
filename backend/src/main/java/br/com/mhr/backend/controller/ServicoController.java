package br.com.mhr.backend.controller;

import java.util.List;

import br.com.mhr.backend.dto.ServicoDto;
import br.com.mhr.backend.service.ServicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints públicos de serviços ({@code /api/servicos}), só retornam itens publicados. */
@RestController
@RequestMapping("/api/servicos")
@RequiredArgsConstructor
public class ServicoController {

    private final ServicoService service;

    @GetMapping
    public List<ServicoDto> listarPublicados() {
        return service.listarPublicados();
    }

    @GetMapping("/{slug}")
    public ServicoDto buscarPorSlug(@PathVariable String slug) {
        return service.buscarPorSlug(slug);
    }
}
