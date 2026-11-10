package br.com.mhr.backend.service;

import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.common.RecursoNaoEncontradoException;
import br.com.mhr.backend.dto.CandidaturaDto;
import br.com.mhr.backend.dto.request.AtualizarStatusCandidaturaRequest;
import br.com.mhr.backend.dto.request.CriarCandidaturaRequest;
import br.com.mhr.backend.mapper.CandidaturaMapper;
import br.com.mhr.backend.model.Candidatura;
import br.com.mhr.backend.model.Vaga;
import br.com.mhr.backend.repository.CandidaturaRepository;
import br.com.mhr.backend.repository.VagaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Regras de negócio de {@link Candidatura}. */
@Service
@RequiredArgsConstructor
public class CandidaturaService {

    private final CandidaturaRepository repository;
    private final VagaRepository vagaRepository;
    private final CandidaturaMapper mapper;
    private final TurnstileService turnstileService;

    public CandidaturaDto candidatar(UUID vagaId, CriarCandidaturaRequest request) {
        if (!turnstileService.verificar(request.turnstileToken())) {
            throw new IllegalArgumentException("Verificacao de seguranca falhou. Tente novamente.");
        }

        Vaga vaga = vagaRepository.findById(vagaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Vaga nao encontrada: " + vagaId));

        if (vaga.getStatus() != Vaga.StatusVaga.ABERTA) {
            throw new IllegalArgumentException("Esta vaga nao esta mais aberta para candidaturas");
        }

        Candidatura candidatura = mapper.toEntity(request);
        candidatura.setVagaId(vagaId);
        return mapper.toDto(repository.save(candidatura));
    }

    public List<CandidaturaDto> listar(UUID vagaId) {
        List<Candidatura> candidaturas = vagaId != null
                ? repository.findByVagaId(vagaId)
                : repository.findAll();

        return candidaturas.stream().map(mapper::toDto).toList();
    }

    public CandidaturaDto atualizarStatus(UUID id, AtualizarStatusCandidaturaRequest request) {
        Candidatura candidatura = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Candidatura nao encontrada: " + id));
        candidatura.setStatus(request.status());
        return mapper.toDto(repository.save(candidatura));
    }
}
