package br.com.mhr.backend.service;

import java.util.Optional;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CandidaturaServiceTest {

    @Mock
    private CandidaturaRepository repository;
    @Mock
    private VagaRepository vagaRepository;
    @Mock
    private CandidaturaMapper mapper;
    @Mock
    private TurnstileService turnstileService;

    @InjectMocks
    private CandidaturaService service;

    private final CriarCandidaturaRequest request =
            new CriarCandidaturaRequest("Fulano", "fulano@exemplo.com", null, "http://curriculo", true, "token-valido");

    @Test
    void candidatar_comTokenDoTurnstileInvalido_lancaIllegalArgumentExceptionSemSalvar() {
        UUID vagaId = UUID.randomUUID();
        when(turnstileService.verificar("token-invalido")).thenReturn(false);
        CriarCandidaturaRequest requestComTokenInvalido =
                new CriarCandidaturaRequest("Fulano", "fulano@exemplo.com", null, "http://curriculo", true, "token-invalido");

        assertThatThrownBy(() -> service.candidatar(vagaId, requestComTokenInvalido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Verificacao de seguranca");

        verify(repository, never()).save(any());
        verify(vagaRepository, never()).findById(any());
    }

    @Test
    void candidatar_emVagaEncerrada_lancaIllegalArgumentExceptionSemSalvar() {
        UUID vagaId = UUID.randomUUID();
        when(turnstileService.verificar("token-valido")).thenReturn(true);
        Vaga vaga = new Vaga();
        vaga.setStatus(Vaga.StatusVaga.ENCERRADA);
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));

        assertThatThrownBy(() -> service.candidatar(vagaId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nao esta mais aberta");

        verify(repository, never()).save(any());
    }

    @Test
    void candidatar_emVagaInexistente_lancaRecursoNaoEncontradoException() {
        UUID vagaId = UUID.randomUUID();
        when(turnstileService.verificar("token-valido")).thenReturn(true);
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.candidatar(vagaId, request))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void candidatar_emVagaAberta_salvaComOIdDaVaga() {
        UUID vagaId = UUID.randomUUID();
        when(turnstileService.verificar("token-valido")).thenReturn(true);
        Vaga vaga = new Vaga();
        vaga.setStatus(Vaga.StatusVaga.ABERTA);
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(vaga));
        Candidatura entidade = new Candidatura();
        when(mapper.toEntity(request)).thenReturn(entidade);
        when(repository.save(entidade)).thenReturn(entidade);
        when(mapper.toDto(entidade)).thenReturn(
                new CandidaturaDto(null, vagaId, "Fulano", "fulano@exemplo.com", null, "http://curriculo", true,
                        Candidatura.StatusCandidatura.NOVA, null));

        service.candidatar(vagaId, request);

        org.mockito.ArgumentCaptor<Candidatura> captor = org.mockito.ArgumentCaptor.forClass(Candidatura.class);
        verify(repository).save(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getVagaId()).isEqualTo(vagaId);
    }

    @Test
    void atualizarStatus_candidaturaInexistente_lancaRecursoNaoEncontradoException() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizarStatus(id, new AtualizarStatusCandidaturaRequest(Candidatura.StatusCandidatura.APROVADO)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void atualizarStatus_candidaturaExistente_atualizaESalva() {
        UUID id = UUID.randomUUID();
        Candidatura candidatura = new Candidatura();
        candidatura.setStatus(Candidatura.StatusCandidatura.NOVA);
        when(repository.findById(id)).thenReturn(Optional.of(candidatura));
        when(repository.save(candidatura)).thenReturn(candidatura);
        when(mapper.toDto(candidatura)).thenReturn(
                new CandidaturaDto(id, UUID.randomUUID(), "Fulano", "fulano@exemplo.com", null, "http://curriculo", true,
                        Candidatura.StatusCandidatura.EM_ANALISE, null));

        service.atualizarStatus(id, new AtualizarStatusCandidaturaRequest(Candidatura.StatusCandidatura.EM_ANALISE));

        org.assertj.core.api.Assertions.assertThat(candidatura.getStatus()).isEqualTo(Candidatura.StatusCandidatura.EM_ANALISE);
        verify(repository).save(candidatura);
    }
}
