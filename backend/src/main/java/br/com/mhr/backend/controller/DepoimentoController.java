package br.com.mhr.backend.controller;

import java.util.List;

import br.com.mhr.backend.dto.DepoimentoDto;
import br.com.mhr.backend.service.DepoimentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint público de depoimentos ({@code /api/depoimentos}), só retorna itens publicados. */
@RestController
@RequestMapping("/api/depoimentos")
@RequiredArgsConstructor
public class DepoimentoController {

    private final DepoimentoService service;

    @GetMapping
    public List<DepoimentoDto> listarPublicados() {
        return service.listarPublicados();
    }
}
