package br.com.mhr.backend.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.common.RecursoNaoEncontradoException;
import br.com.mhr.backend.dto.LeadContatoDto;
import br.com.mhr.backend.dto.request.AtualizarClienteLeadRequest;
import br.com.mhr.backend.dto.request.AtualizarStatusLeadRequest;
import br.com.mhr.backend.dto.request.CriarLeadContatoRequest;
import br.com.mhr.backend.mapper.LeadContatoMapper;
import br.com.mhr.backend.model.LeadContato;
import br.com.mhr.backend.repository.LeadContatoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Regras de negócio de {@link LeadContato}, usadas pelos controllers público e admin. */
@Service
@RequiredArgsConstructor
public class LeadContatoService {

    private final LeadContatoRepository repository;
    private final LeadContatoMapper mapper;
    private final TurnstileService turnstileService;

    public LeadContatoDto criar(CriarLeadContatoRequest request) {
        if (!turnstileService.verificar(request.turnstileToken())) {
            throw new IllegalArgumentException("Verificacao de seguranca falhou. Tente novamente.");
        }

        LeadContato lead = mapper.toEntity(request);
        return mapper.toDto(repository.save(lead));
    }

    public List<LeadContatoDto> listarTodos() {
        return repository.findAll().stream().map(mapper::toDto).toList();
    }

    public LeadContatoDto atualizarStatus(UUID id, AtualizarStatusLeadRequest request) {
        LeadContato lead = buscarEntidadeOuFalhar(id);

        lead.setStatus(request.status());
        if (request.status() == LeadContato.StatusLead.CONCLUIDO && lead.getConcluidoEm() == null) {
            lead.setConcluidoEm(Instant.now());
        } else if (request.status() != LeadContato.StatusLead.CONCLUIDO) {
            lead.setConcluidoEm(null);
        }

        return mapper.toDto(repository.save(lead));
    }

    public LeadContatoDto atualizarCliente(UUID id, AtualizarClienteLeadRequest request) {
        LeadContato lead = buscarEntidadeOuFalhar(id);

        if (request.virouContrato() && request.clienteId() == null) {
            throw new IllegalArgumentException("Selecione o cliente vinculado para marcar como contrato");
        }

        lead.setVirouContrato(request.virouContrato());
        lead.setClienteId(request.virouContrato() ? request.clienteId() : null);

        return mapper.toDto(repository.save(lead));
    }

    private LeadContato buscarEntidadeOuFalhar(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lead nao encontrado: " + id));
    }
}
