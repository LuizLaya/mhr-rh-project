package br.com.mhr.backend.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.common.RecursoNaoEncontradoException;
import br.com.mhr.backend.dto.VagaDto;
import br.com.mhr.backend.dto.request.SalvarVagaRequest;
import br.com.mhr.backend.mapper.VagaMapper;
import br.com.mhr.backend.model.Vaga;
import br.com.mhr.backend.repository.VagaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Regras de negócio de {@link Vaga}, usadas pelos controllers público e admin. */
@Service
@RequiredArgsConstructor
public class VagaService {

    private final VagaRepository repository;
    private final VagaMapper mapper;

    public List<VagaDto> listarAbertas() {
        return repository.findAll().stream()
                .filter(vaga -> vaga.getStatus() == Vaga.StatusVaga.ABERTA)
                .map(mapper::toDto)
                .toList();
    }

    public VagaDto buscarPorId(UUID id) {
        return mapper.toDto(buscarEntidadeOuFalhar(id));
    }

    public List<VagaDto> listarTodas() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    public VagaDto criar(SalvarVagaRequest request) {
        Vaga vaga = mapper.toEntity(request);
        if (vaga.getStatus() == Vaga.StatusVaga.ABERTA) {
            vaga.setPublicadoEm(Instant.now());
        }
        return mapper.toDto(repository.save(vaga));
    }

    public VagaDto atualizar(UUID id, SalvarVagaRequest request) {
        Vaga vaga = buscarEntidadeOuFalhar(id);
        mapper.atualizar(request, vaga);
        if (vaga.getStatus() == Vaga.StatusVaga.ABERTA && vaga.getPublicadoEm() == null) {
            vaga.setPublicadoEm(Instant.now());
        }
        return mapper.toDto(repository.save(vaga));
    }

    public void excluir(UUID id) {
        repository.delete(buscarEntidadeOuFalhar(id));
    }

    private Vaga buscarEntidadeOuFalhar(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Vaga nao encontrada: " + id));
    }
}
