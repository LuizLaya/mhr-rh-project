package br.com.mhr.backend.service;

import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.common.RecursoNaoEncontradoException;
import br.com.mhr.backend.dto.FaqDto;
import br.com.mhr.backend.dto.request.SalvarFaqRequest;
import br.com.mhr.backend.mapper.FaqMapper;
import br.com.mhr.backend.model.Faq;
import br.com.mhr.backend.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Regras de negócio de {@link Faq}, usadas pelos controllers público e admin. */
@Service
@RequiredArgsConstructor
public class FaqService {

    private final FaqRepository repository;
    private final FaqMapper mapper;

    public List<FaqDto> listarPublicados() {
        return repository.findByPublicadoTrueOrderByOrdemExibicaoAsc().stream()
                .map(mapper::toDto)
                .toList();
    }

    public List<FaqDto> listarTodos() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    public FaqDto criar(SalvarFaqRequest request) {
        Faq faq = mapper.toEntity(request);
        return mapper.toDto(repository.save(faq));
    }

    public FaqDto atualizar(UUID id, SalvarFaqRequest request) {
        Faq faq = buscarEntidadeOuFalhar(id);
        mapper.atualizar(request, faq);
        return mapper.toDto(repository.save(faq));
    }

    public void excluir(UUID id) {
        repository.delete(buscarEntidadeOuFalhar(id));
    }

    private Faq buscarEntidadeOuFalhar(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pergunta frequente nao encontrada: " + id));
    }
}
