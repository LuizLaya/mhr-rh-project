package br.com.mhr.backend.service;

import java.util.Optional;
import java.util.UUID;

import br.com.mhr.backend.dto.LeadContatoDto;
import br.com.mhr.backend.dto.request.AtualizarClienteLeadRequest;
import br.com.mhr.backend.dto.request.CriarLeadContatoRequest;
import br.com.mhr.backend.mapper.LeadContatoMapper;
import br.com.mhr.backend.model.LeadContato;
import br.com.mhr.backend.repository.LeadContatoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeadContatoServiceTest {

    @Mock
    private LeadContatoRepository repository;
    @Mock
    private LeadContatoMapper mapper;
    @Mock
    private TurnstileService turnstileService;

    @InjectMocks
    private LeadContatoService service;

    private LeadContato lead;
    private UUID leadId;

    @BeforeEach
    void setUp() {
        leadId = UUID.randomUUID();
        lead = new LeadContato();
        lead.setId(leadId);
    }

    private void stubarBuscarLead() {
        when(repository.findById(leadId)).thenReturn(Optional.of(lead));
    }

    private void stubarSalvarEMapear() {
        when(repository.save(any())).thenReturn(lead);
        when(mapper.toDto(any())).thenAnswer(inv -> dtoDe(inv.getArgument(0)));
    }

    @Test
    void criar_comTokenDoTurnstileInvalido_lancaIllegalArgumentExceptionSemSalvar() {
        CriarLeadContatoRequest request = new CriarLeadContatoRequest("Fulano", "fulano@exemplo.com", null, null, null, "token-invalido");
        when(turnstileService.verificar("token-invalido")).thenReturn(false);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Verificacao de seguranca");

        org.mockito.Mockito.verify(repository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void criar_comTokenDoTurnstileValido_salvaOLead() {
        CriarLeadContatoRequest request = new CriarLeadContatoRequest("Fulano", "fulano@exemplo.com", null, null, null, "token-valido");
        when(turnstileService.verificar("token-valido")).thenReturn(true);
        LeadContato entidade = new LeadContato();
        when(mapper.toEntity(request)).thenReturn(entidade);
        when(repository.save(entidade)).thenReturn(entidade);
        when(mapper.toDto(entidade)).thenReturn(dtoDe(entidade));

        service.criar(request);

        org.mockito.Mockito.verify(repository).save(entidade);
    }

    @Test
    void atualizarCliente_marcandoVirouContratoSemClienteId_lancaIllegalArgumentException() {
        stubarBuscarLead();
        AtualizarClienteLeadRequest request = new AtualizarClienteLeadRequest(true, null);

        assertThatThrownBy(() -> service.atualizarCliente(leadId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Selecione o cliente");
    }

    @Test
    void atualizarCliente_marcandoVirouContratoComClienteId_vinculaOCliente() {
        stubarBuscarLead();
        stubarSalvarEMapear();
        UUID clienteId = UUID.randomUUID();
        AtualizarClienteLeadRequest request = new AtualizarClienteLeadRequest(true, clienteId);

        service.atualizarCliente(leadId, request);

        assertThat(lead.isVirouContrato()).isTrue();
        assertThat(lead.getClienteId()).isEqualTo(clienteId);
    }

    @Test
    void atualizarCliente_desmarcandoVirouContrato_limpaOClienteIdMesmoSeEnviado() {
        stubarBuscarLead();
        stubarSalvarEMapear();
        lead.setVirouContrato(true);
        lead.setClienteId(UUID.randomUUID());
        AtualizarClienteLeadRequest request = new AtualizarClienteLeadRequest(false, UUID.randomUUID());

        service.atualizarCliente(leadId, request);

        assertThat(lead.isVirouContrato()).isFalse();
        assertThat(lead.getClienteId()).isNull();
    }

    private LeadContatoDto dtoDe(LeadContato entity) {
        return new LeadContatoDto(entity.getId(), null, null, null, null, null, entity.getStatus(),
                entity.getConcluidoEm(), entity.isVirouContrato(), entity.getClienteId(), entity.getCriadoEm());
    }
}
