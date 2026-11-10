package br.com.mhr.backend.controller;

import java.util.List;

import br.com.mhr.backend.dto.MembroEquipeDto;
import br.com.mhr.backend.service.MembroEquipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint público da equipe ({@code /api/equipe}), exibida na página Sobre do site. */
@RestController
@RequestMapping("/api/equipe")
@RequiredArgsConstructor
public class EquipeController {

    private final MembroEquipeService service;

    @GetMapping
    public List<MembroEquipeDto> listarPublicados() {
        return service.listarPublicados();
    }
}
