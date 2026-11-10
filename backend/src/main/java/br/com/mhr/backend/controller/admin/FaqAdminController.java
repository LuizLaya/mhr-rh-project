package br.com.mhr.backend.controller.admin;

import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.dto.FaqDto;
import br.com.mhr.backend.dto.request.SalvarFaqRequest;
import br.com.mhr.backend.service.FaqService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** CRUD de perguntas frequentes para o painel admin (protegido por JWT, ver SecurityConfig). */
@RestController
@RequestMapping("/api/admin/faq")
@RequiredArgsConstructor
public class FaqAdminController {

    private final FaqService service;

    @GetMapping
    public List<FaqDto> listarTodos() {
        return service.listarTodos();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FaqDto criar(@Valid @RequestBody SalvarFaqRequest request) {
        return service.criar(request);
    }

    @PutMapping("/{id}")
    public FaqDto atualizar(@PathVariable UUID id, @Valid @RequestBody SalvarFaqRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable UUID id) {
        service.excluir(id);
    }
}
