package br.com.mhr.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.mhr.backend.common.RecursoNaoEncontradoException;
import br.com.mhr.backend.dto.ConfiguracaoLicitacoesDto;
import br.com.mhr.backend.dto.LicitacaoDto;
import br.com.mhr.backend.dto.request.SalvarLicitacaoRequest;
import br.com.mhr.backend.mapper.DocumentoLicitacaoMapper;
import br.com.mhr.backend.mapper.LicitacaoMapper;
import br.com.mhr.backend.model.ConfiguracaoLicitacoes;
import br.com.mhr.backend.model.Licitacao;
import br.com.mhr.backend.repository.ConfiguracaoLicitacoesRepository;
import br.com.mhr.backend.repository.DocumentoLicitacaoRepository;
import br.com.mhr.backend.repository.LicitacaoRepository;
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
class LicitacaoServiceTest {

    @Mock
    private LicitacaoRepository repository;
    @Mock
    private LicitacaoMapper mapper;
    @Mock
    private DocumentoLicitacaoRepository documentoRepository;
    @Mock
    private DocumentoLicitacaoMapper documentoMapper;
    @Mock
    private ConfiguracaoLicitacoesRepository configuracaoRepository;

    @InjectMocks
    private LicitacaoService service;

    private final SalvarLicitacaoRequest request = new SalvarLicitacaoRequest(
            "Ministério da Economia", "Contratação de serviços de recrutamento", "Pregão", "001/2026",
            "DF", null, null, null, null, null);

    @Test
    void criar_sempreMarcaOrigemComoManual() {
        Licitacao entidade = new Licitacao();
        when(mapper.toEntity(request)).thenReturn(entidade);
        when(repository.save(entidade)).thenReturn(entidade);
        when(mapper.toDto(entidade)).thenReturn(new LicitacaoDto(
                null, null, Licitacao.Origem.MANUAL, "Ministério da Economia", "...", "Pregão", "001/2026",
                "DF", null, null, null, null, null, Licitacao.StatusLicitacao.IDENTIFICADA, null, null));

        LicitacaoDto resultado = service.criar(request);

        assertThat(entidade.getOrigem()).isEqualTo(Licitacao.Origem.MANUAL);
        assertThat(resultado.origem()).isEqualTo(Licitacao.Origem.MANUAL);
    }

    @Test
    void atualizarStatus_comIdInexistente_lancaRecursoNaoEncontradoException() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizarStatus(id, Licitacao.StatusLicitacao.EM_ANALISE))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void atualizarResponsavel_definesNoResponsavelId() {
        UUID id = UUID.randomUUID();
        UUID responsavelId = UUID.randomUUID();
        Licitacao licitacao = new Licitacao();
        when(repository.findById(id)).thenReturn(Optional.of(licitacao));
        when(repository.save(licitacao)).thenReturn(licitacao);
        when(mapper.toDto(licitacao)).thenReturn(new LicitacaoDto(
                id, null, Licitacao.Origem.MANUAL, null, null, null, null, null, null, null, null, null,
                responsavelId, Licitacao.StatusLicitacao.IDENTIFICADA, null, null));

        LicitacaoDto resultado = service.atualizarResponsavel(id, responsavelId);

        assertThat(licitacao.getResponsavelId()).isEqualTo(responsavelId);
        assertThat(resultado.responsavelId()).isEqualTo(responsavelId);
    }

    @Test
    void listar_comStatusInformado_filtraPorStatus() {
        when(repository.findByStatus(Licitacao.StatusLicitacao.GANHOU)).thenReturn(List.of());

        service.listar(Licitacao.StatusLicitacao.GANHOU);

        org.mockito.Mockito.verify(repository).findByStatus(Licitacao.StatusLicitacao.GANHOU);
        org.mockito.Mockito.verify(repository, org.mockito.Mockito.never()).findAll();
    }

    @Test
    void listar_semStatus_usaOTetoDeSeguranca() {
        when(repository.findTop500ByOrderByCriadoEmDesc()).thenReturn(List.of());

        service.listar(null);

        org.mockito.Mockito.verify(repository).findTop500ByOrderByCriadoEmDesc();
        org.mockito.Mockito.verify(repository, org.mockito.Mockito.never()).findAll();
    }

    @Test
    void atualizarConfiguracao_persisteOsNovosValores() {
        ConfiguracaoLicitacoes configuracao = new ConfiguracaoLicitacoes();
        configuracao.setId(ConfiguracaoLicitacoes.ID_UNICO);
        configuracao.setBuscaPncpAtiva(false);
        configuracao.setPalavrasChave("recrutamento");
        when(configuracaoRepository.findById(ConfiguracaoLicitacoes.ID_UNICO)).thenReturn(Optional.of(configuracao));

        ConfiguracaoLicitacoesDto resultado = service.atualizarConfiguracao(true, "recrutamento,treinamento");

        assertThat(configuracao.isBuscaPncpAtiva()).isTrue();
        assertThat(configuracao.getPalavrasChave()).isEqualTo("recrutamento,treinamento");
        assertThat(resultado.buscaPncpAtiva()).isTrue();
        assertThat(resultado.palavrasChave()).isEqualTo("recrutamento,treinamento");
    }
}
