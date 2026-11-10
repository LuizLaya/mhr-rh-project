package br.com.mhr.backend.service;

import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.common.RecursoNaoEncontradoException;
import br.com.mhr.backend.dto.ServicoDto;
import br.com.mhr.backend.dto.request.SalvarServicoRequest;
import br.com.mhr.backend.mapper.ServicoMapper;
import br.com.mhr.backend.model.Servico;
import br.com.mhr.backend.repository.ServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Regras de negócio de {@link Servico}, usadas pelos controllers público e admin. */
@Service
@RequiredArgsConstructor
public class ServicoService {

    private final ServicoRepository repository;
    private final ServicoMapper mapper;

    public List<ServicoDto> listarPublicados() {
        return repository.findByPublicadoTrueOrderByOrdemExibicaoAsc().stream()
                .map(mapper::toDto)
                .toList();
    }

    public ServicoDto buscarPorSlug(String slug) {
        Servico servico = repository.findBySlugAndPublicadoTrue(slug)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Servico nao encontrado: " + slug));
        return mapper.toDto(servico);
    }

    public List<ServicoDto> listarTodos() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    public ServicoDto criar(SalvarServicoRequest request) {
        Servico servico = mapper.toEntity(request);
        return mapper.toDto(repository.save(servico));
    }

    public ServicoDto atualizar(UUID id, SalvarServicoRequest request) {
        Servico servico = buscarEntidadeOuFalhar(id);
        mapper.atualizar(request, servico);
        return mapper.toDto(repository.save(servico));
    }

    public void excluir(UUID id) {
        repository.delete(buscarEntidadeOuFalhar(id));
    }

    private Servico buscarEntidadeOuFalhar(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Servico nao encontrado: " + id));
    }
}
