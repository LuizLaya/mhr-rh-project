package br.com.mhr.backend.service;

import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.common.RecursoNaoEncontradoException;
import br.com.mhr.backend.dto.ClienteDestaqueDto;
import br.com.mhr.backend.dto.ClienteDto;
import br.com.mhr.backend.dto.request.SalvarClienteRequest;
import br.com.mhr.backend.mapper.ClienteMapper;
import br.com.mhr.backend.model.Cliente;
import br.com.mhr.backend.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/** Regras de negócio de {@link Cliente}. */
@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository repository;
    private final ClienteMapper mapper;

    public List<ClienteDto> listarTodos() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    /** Lista paginada, com busca opcional por empresa/contato - usada pela tela principal de Clientes. */
    public Page<ClienteDto> listarPaginado(String busca, Pageable pageable) {
        return repository.buscarPaginado(busca, pageable).map(mapper::toDto);
    }

    /** Clientes marcados pelo admin para aparecer na home do site (nome + logo, nunca dado sensível). */
    public List<ClienteDestaqueDto> listarDestaques() {
        return repository.findByExibirNoSiteTrueOrderByNomeEmpresa().stream()
                .map(cliente -> new ClienteDestaqueDto(cliente.getNomeEmpresa(), cliente.getLogoUrl()))
                .toList();
    }

    public ClienteDto criar(SalvarClienteRequest request) {
        Cliente cliente = mapper.toEntity(request);
        return mapper.toDto(repository.save(cliente));
    }

    public ClienteDto atualizar(UUID id, SalvarClienteRequest request) {
        Cliente cliente = buscarEntidadeOuFalhar(id);
        mapper.atualizar(request, cliente);
        return mapper.toDto(repository.save(cliente));
    }

    public void excluir(UUID id) {
        repository.delete(buscarEntidadeOuFalhar(id));
    }

    private Cliente buscarEntidadeOuFalhar(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado: " + id));
    }
}
