package br.com.mhr.backend.controller.admin;

import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.dto.DepoimentoDto;
import br.com.mhr.backend.dto.request.SalvarDepoimentoRequest;
import br.com.mhr.backend.service.DepoimentoService;
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

/** CRUD de depoimentos para o painel admin (protegido por JWT, ver SecurityConfig). */
@RestController
@RequestMapping("/api/admin/depoimentos")
@RequiredArgsConstructor
public class DepoimentoAdminController {

    private final DepoimentoService service;

    @GetMapping
    public List<DepoimentoDto> listarTodos() {
        return service.listarTodos();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepoimentoDto criar(@Valid @RequestBody SalvarDepoimentoRequest request) {
        return service.criar(request);
    }

    @PutMapping("/{id}")
    public DepoimentoDto atualizar(@PathVariable UUID id, @Valid @RequestBody SalvarDepoimentoRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable UUID id) {
        service.excluir(id);
    }
}
