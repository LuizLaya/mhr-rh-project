package br.com.mhr.backend.controller.admin;

import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.dto.VagaDto;
import br.com.mhr.backend.dto.request.SalvarVagaRequest;
import br.com.mhr.backend.service.VagaService;
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

/** CRUD de vagas para o painel admin (protegido por JWT, ver SecurityConfig). */
@RestController
@RequestMapping("/api/admin/vagas")
@RequiredArgsConstructor
public class VagaAdminController {

    private final VagaService service;

    @GetMapping
    public List<VagaDto> listarTodas() {
        return service.listarTodas();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VagaDto criar(@Valid @RequestBody SalvarVagaRequest request) {
        return service.criar(request);
    }

    @PutMapping("/{id}")
    public VagaDto atualizar(@PathVariable UUID id, @Valid @RequestBody SalvarVagaRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable UUID id) {
        service.excluir(id);
    }
}
