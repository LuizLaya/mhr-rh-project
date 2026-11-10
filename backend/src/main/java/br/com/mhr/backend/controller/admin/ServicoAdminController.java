package br.com.mhr.backend.controller.admin;

import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.dto.ServicoDto;
import br.com.mhr.backend.dto.request.SalvarServicoRequest;
import br.com.mhr.backend.service.ServicoService;
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

/** CRUD de serviços para o painel admin (protegido por JWT, ver SecurityConfig). */
@RestController
@RequestMapping("/api/admin/servicos")
@RequiredArgsConstructor
public class ServicoAdminController {

    private final ServicoService service;

    @GetMapping
    public List<ServicoDto> listarTodos() {
        return service.listarTodos();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServicoDto criar(@Valid @RequestBody SalvarServicoRequest request) {
        return service.criar(request);
    }

    @PutMapping("/{id}")
    public ServicoDto atualizar(@PathVariable UUID id, @Valid @RequestBody SalvarServicoRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable UUID id) {
        service.excluir(id);
    }
}
