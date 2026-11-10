package br.com.mhr.backend.controller.admin;

import br.com.mhr.backend.dto.UploadDto;
import br.com.mhr.backend.service.UploadService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** Upload de imagens (foto de depoimento, capa de post, logo de cliente) para o painel admin. */
@RestController
@RequestMapping("/api/admin/uploads")
@RequiredArgsConstructor
public class UploadAdminController {

    private final UploadService service;

    // O site publico e o admin rodam em origens diferentes do backend - a URL
    // devolvida precisa ser absoluta (com esquema+host), nunca relativa, senao quem
    // consome (ex.: o site buscando a foto de um depoimento) resolveria contra a
    // propria origem, nao a da API. UploadService decide se usa esse baseUrl (caso
    // caia no fallback de disco local) ou devolve a URL publica do R2 (ja absoluta).
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UploadDto enviar(@RequestParam("arquivo") MultipartFile arquivo, HttpServletRequest request) {
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request).replacePath(null).build().toUriString();
        return service.salvar(arquivo, baseUrl);
    }
}
