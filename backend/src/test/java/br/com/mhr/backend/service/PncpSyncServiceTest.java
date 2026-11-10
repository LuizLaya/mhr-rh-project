package br.com.mhr.backend.service;

import java.util.List;
import java.util.Optional;

import br.com.mhr.backend.model.ConfiguracaoLicitacoes;
import br.com.mhr.backend.repository.ConfiguracaoLicitacoesRepository;
import br.com.mhr.backend.repository.LicitacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PncpSyncServiceTest {

    @Mock
    private RestClient pncpRestClient;
    @Mock
    private LicitacaoRepository licitacaoRepository;
    @Mock
    private ConfiguracaoLicitacoesRepository configuracaoRepository;
    @Mock
    private LogSistemaService logSistemaService;

    private PncpSyncService service;

    @BeforeEach
    void setUp() {
        service = new PncpSyncService(pncpRestClient, licitacaoRepository, configuracaoRepository, logSistemaService);
        ReflectionTestUtils.setField(service, "ufsConfiguradas", "DF");
    }

    @Test
    void sincronizarAgora_comBuscaDesativada_lancaExcecaoSemChamarAApi() {
        ConfiguracaoLicitacoes configuracao = new ConfiguracaoLicitacoes();
        configuracao.setBuscaPncpAtiva(false);
        when(configuracaoRepository.findById(ConfiguracaoLicitacoes.ID_UNICO)).thenReturn(Optional.of(configuracao));

        assertThatThrownBy(service::sincronizarAgora)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("desativada");

        verifyNoInteractions(pncpRestClient, licitacaoRepository);
    }

    @Test
    void sincronizarAgendado_comBuscaDesativada_naoFazNada() {
        when(configuracaoRepository.findById(ConfiguracaoLicitacoes.ID_UNICO)).thenReturn(Optional.empty());

        service.sincronizarAgendado();

        verifyNoInteractions(pncpRestClient, licitacaoRepository);
    }

    @Test
    void combinaComPalavraChave_objetoComTermoDeRh_retornaTrue() {
        boolean resultado = service.combinaComPalavraChave(
                "Contratação de empresa especializada em RECRUTAMENTO e seleção de pessoal",
                List.of("recursos humanos", "recrutamento"));

        assertThat(resultado).isTrue();
    }

    @Test
    void combinaComPalavraChave_objetoSemNenhumTermo_retornaFalse() {
        boolean resultado = service.combinaComPalavraChave(
                "Aquisição de material de escritório",
                List.of("recursos humanos", "recrutamento"));

        assertThat(resultado).isFalse();
    }

    @Test
    void combinaComPalavraChave_objetoNuloOuVazio_retornaFalse() {
        assertThat(service.combinaComPalavraChave(null, List.of("recrutamento"))).isFalse();
        assertThat(service.combinaComPalavraChave("", List.of("recrutamento"))).isFalse();
    }

    @Test
    void combinaComPalavraChave_novasPalavrasAvaliacaoEDesenvolvimentoOrganizacional_combinam() {
        List<String> palavras = List.of("avaliação de desempenho", "desenvolvimento organizacional");

        assertThat(service.combinaComPalavraChave(
                "Contratação de consultoria para avaliação de desempenho de servidores", palavras)).isTrue();
        assertThat(service.combinaComPalavraChave(
                "Serviços de desenvolvimento organizacional e clima institucional", palavras)).isTrue();
        assertThat(service.combinaComPalavraChave(
                "Aquisição de equipamento de informática", palavras)).isFalse();
    }
}
