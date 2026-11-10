package br.com.mhr.backend.service;

import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.common.RecursoNaoEncontradoException;
import br.com.mhr.backend.dto.DepoimentoDto;
import br.com.mhr.backend.dto.request.SalvarDepoimentoRequest;
import br.com.mhr.backend.mapper.DepoimentoMapper;
import br.com.mhr.backend.model.Depoimento;
import br.com.mhr.backend.repository.DepoimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Regras de negócio de {@link Depoimento}, usadas pelos controllers público e admin. */
@Service
@RequiredArgsConstructor
public class DepoimentoService {

    private final DepoimentoRepository repository;
    private final DepoimentoMapper mapper;

    public List<DepoimentoDto> listarPublicados() {
        return repository.findByPublicadoTrueOrderByOrdemExibicaoAsc().stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<DepoimentoDto> listarTodos() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    public DepoimentoDto criar(SalvarDepoimentoRequest request) {
        Depoimento depoimento = mapper.toEntity(request);
        return mapper.toDto(repository.save(depoimento));
    }

    public DepoimentoDto atualizar(UUID id, SalvarDepoimentoRequest request) {
        Depoimento depoimento = buscarEntidadeOuFalhar(id);
        mapper.atualizar(request, depoimento);
        return mapper.toDto(repository.save(depoimento));
    }

    public void excluir(UUID id) {
        repository.delete(buscarEntidadeOuFalhar(id));
    }

    private Depoimento buscarEntidadeOuFalhar(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Depoimento nao encontrado: " + id));
    }
}
