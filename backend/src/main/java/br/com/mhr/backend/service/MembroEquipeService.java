package br.com.mhr.backend.service;

import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.common.RecursoNaoEncontradoException;
import br.com.mhr.backend.dto.MembroEquipeDto;
import br.com.mhr.backend.dto.request.SalvarMembroEquipeRequest;
import br.com.mhr.backend.mapper.MembroEquipeMapper;
import br.com.mhr.backend.model.MembroEquipe;
import br.com.mhr.backend.repository.MembroEquipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Regras de negócio de {@link MembroEquipe}, usadas pelos controllers público e admin. */
@Service
@RequiredArgsConstructor
public class MembroEquipeService {

    private final MembroEquipeRepository repository;
    private final MembroEquipeMapper mapper;

    public List<MembroEquipeDto> listarPublicados() {
        return repository.findByPublicadoTrueOrderByOrdemExibicaoAsc().stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<MembroEquipeDto> listarTodos() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    public MembroEquipeDto criar(SalvarMembroEquipeRequest request) {
        MembroEquipe membro = mapper.toEntity(request);
        return mapper.toDto(repository.save(membro));
    }

    public MembroEquipeDto atualizar(UUID id, SalvarMembroEquipeRequest request) {
        MembroEquipe membro = buscarEntidadeOuFalhar(id);
        mapper.atualizar(request, membro);
        return mapper.toDto(repository.save(membro));
    }

    public void excluir(UUID id) {
        repository.delete(buscarEntidadeOuFalhar(id));
    }

    private MembroEquipe buscarEntidadeOuFalhar(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Membro de equipe nao encontrado: " + id));
    }
}
