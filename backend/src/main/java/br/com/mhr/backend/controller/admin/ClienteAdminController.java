package br.com.mhr.backend.controller.admin;

import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.dto.ClienteDto;
import br.com.mhr.backend.dto.request.SalvarClienteRequest;
import br.com.mhr.backend.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** CRUD de clientes para o painel admin (protegido por JWT, ver SecurityConfig). */
@RestController
@RequestMapping("/api/admin/clientes")
@RequiredArgsConstructor
public class ClienteAdminController {

    private final ClienteService service;

    // Lista completa, sem paginacao - usada pelos seletores de cliente em Depoimentos
    // e Mensagens de contato (precisam de todos pra montar o <select>). A tela
    // principal de Clientes usa /paginado abaixo.
    @GetMapping
    public List<ClienteDto> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/paginado")
    public Page<ClienteDto> listarPaginado(
            @RequestParam(required = false) String busca,
            @PageableDefault(size = 20, sort = "nomeEmpresa") Pageable pageable) {
        return service.listarPaginado(busca, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteDto criar(@Valid @RequestBody SalvarClienteRequest request) {
        return service.criar(request);
    }

    @PutMapping("/{id}")
    public ClienteDto atualizar(@PathVariable UUID id, @Valid @RequestBody SalvarClienteRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable UUID id) {
        service.excluir(id);
    }
}
