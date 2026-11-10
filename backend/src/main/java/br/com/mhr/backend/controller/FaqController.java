package br.com.mhr.backend.controller;

import java.util.List;

import br.com.mhr.backend.dto.FaqDto;
import br.com.mhr.backend.service.FaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint público das perguntas frequentes ({@code /api/faq}), exibidas na home do site. */
@RestController
@RequestMapping("/api/faq")
@RequiredArgsConstructor
public class FaqController {

    private final FaqService service;

    @GetMapping
    public List<FaqDto> listarPublicados() {
        return service.listarPublicados();
    }
}
