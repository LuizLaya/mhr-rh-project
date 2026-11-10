package br.com.mhr.backend.service;

import java.time.Instant;

import br.com.mhr.backend.model.LogSistema;
import br.com.mhr.backend.repository.LogSistemaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogSistemaServiceTest {

    @Mock
    private LogSistemaRepository repository;

    @InjectMocks
    private LogSistemaService service;

    @Test
    void erro_salvaComNivelErroECategoriaInformada() {
        ArgumentCaptor<LogSistema> captor = ArgumentCaptor.forClass(LogSistema.class);

        service.erro("EMAIL", "falha ao enviar", "destino@mhr.com.br");

        verify(repository).save(captor.capture());
        LogSistema salvo = captor.getValue();
        assertThat(salvo.getNivel()).isEqualTo(LogSistema.Nivel.ERRO);
        assertThat(salvo.getCategoria()).isEqualTo("EMAIL");
        assertThat(salvo.getUsuarioEmail()).isEqualTo("destino@mhr.com.br");
    }

    @Test
    void erro_capturaAutomaticamenteAClasseEMetodoDeQuemChamou() {
        ArgumentCaptor<LogSistema> captor = ArgumentCaptor.forClass(LogSistema.class);

        service.erro("EMAIL", "falha ao enviar", null);

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getOrigem())
                .isEqualTo("LogSistemaServiceTest.erro_capturaAutomaticamenteAClasseEMetodoDeQuemChamou");
    }

    @Test
    void erro_comEndpoint_salvaOEndpointInformado() {
        ArgumentCaptor<LogSistema> captor = ArgumentCaptor.forClass(LogSistema.class);

        service.erro("SISTEMA", "NullPointerException: null", null, "POST /api/admin/licitacoes/sincronizar");

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getEndpoint()).isEqualTo("POST /api/admin/licitacoes/sincronizar");
    }

    @Test
    void erro_semEndpoint_naoPreencheOCampo() {
        ArgumentCaptor<LogSistema> captor = ArgumentCaptor.forClass(LogSistema.class);

        service.erro("LICITACAO_SYNC", "429 ao chamar PNCP", null);

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getEndpoint()).isNull();
    }

    @Test
    void semCorrelacaoIniciada_naoPreencheOCampo() {
        ArgumentCaptor<LogSistema> captor = ArgumentCaptor.forClass(LogSistema.class);

        service.info("LICITACAO_SYNC", "sincronizacao concluida", null);

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getCorrelacaoId()).isNull();
    }

    @Test
    void comCorrelacaoIniciada_marcaTodasAsLinhasComOMesmoId() {
        ArgumentCaptor<LogSistema> captor = ArgumentCaptor.forClass(LogSistema.class);

        String correlacaoId = service.iniciarCorrelacao();
        service.info("LICITACAO_SYNC", "UF 1 analisada", null);
        service.info("LICITACAO_SYNC", "UF 2 analisada", null);
        service.encerrarCorrelacao();
        service.info("LICITACAO_SYNC", "fora da correlacao", null);

        verify(repository, org.mockito.Mockito.times(3)).save(captor.capture());
        java.util.List<LogSistema> salvos = captor.getAllValues();
        assertThat(salvos.get(0).getCorrelacaoId()).isEqualTo(correlacaoId);
        assertThat(salvos.get(1).getCorrelacaoId()).isEqualTo(correlacaoId);
        assertThat(salvos.get(2).getCorrelacaoId()).isNull();
    }

    @Test
    void limparAntigos_apagaRegistrosComMaisDeSeteDias() {
        ArgumentCaptor<Instant> captor = ArgumentCaptor.forClass(Instant.class);
        when(repository.deleteByCriadoEmBefore(captor.capture())).thenReturn(3L);

        service.limparAntigos();

        Instant esperado = Instant.now().minus(7, java.time.temporal.ChronoUnit.DAYS);
        assertThat(captor.getValue()).isCloseTo(esperado, within(5, java.time.temporal.ChronoUnit.SECONDS));
    }
}
