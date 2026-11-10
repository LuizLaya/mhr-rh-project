package br.com.mhr.backend.controller.admin;

import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.common.ApiExceptionHandler;
import br.com.mhr.backend.common.RecursoNaoEncontradoException;
import br.com.mhr.backend.dto.ConfiguracaoLicitacoesDto;
import br.com.mhr.backend.dto.DocumentoLicitacaoDto;
import br.com.mhr.backend.dto.LicitacaoDto;
import br.com.mhr.backend.dto.request.AtualizarConfiguracaoLicitacoesRequest;
import br.com.mhr.backend.dto.request.AtualizarDocumentoLicitacaoRequest;
import br.com.mhr.backend.dto.request.AtualizarStatusLicitacaoRequest;
import br.com.mhr.backend.dto.request.SalvarDocumentoLicitacaoRequest;
import br.com.mhr.backend.dto.request.SalvarLicitacaoRequest;
import br.com.mhr.backend.model.Licitacao;
import br.com.mhr.backend.service.LicitacaoService;
import br.com.mhr.backend.service.LogSistemaService;
import br.com.mhr.backend.service.PncpSyncService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LicitacaoAdminControllerTest {

    @Mock
    private LicitacaoService service;
    @Mock
    private PncpSyncService pncpSyncService;
    @Mock
    private LogSistemaService logSistemaService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new LicitacaoAdminController(service, pncpSyncService))
                .setControllerAdvice(new ApiExceptionHandler(logSistemaService))
                .build();
    }

    private LicitacaoDto dto(UUID id) {
        return new LicitacaoDto(id, "123/2026", Licitacao.Origem.MANUAL, "Prefeitura de Sao Paulo", "Contratacao de servicos de RH",
                "Pregao", "001/2026", "SP", null, null, null, null, null, Licitacao.StatusLicitacao.IDENTIFICADA, null, null);
    }

    private SalvarLicitacaoRequest request() {
        return new SalvarLicitacaoRequest("Prefeitura de Sao Paulo", "Contratacao de servicos de RH", "Pregao", "001/2026", "SP",
                null, null, null, null, null);
    }

    @Test
    void listar_semStatus_devolve200() throws Exception {
        when(service.listar(null)).thenReturn(List.of(dto(UUID.randomUUID())));

        mockMvc.perform(get("/api/admin/licitacoes"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Prefeitura de Sao Paulo")));
    }

    @Test
    void listar_comStatus_devolve200() throws Exception {
        when(service.listar(Licitacao.StatusLicitacao.GANHOU)).thenReturn(List.of(dto(UUID.randomUUID())));

        mockMvc.perform(get("/api/admin/licitacoes").param("status", "GANHOU"))
                .andExpect(status().isOk());
    }

    @Test
    void buscarPorId_idExistente_devolve200() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.buscarPorId(id)).thenReturn(dto(id));

        mockMvc.perform(get("/api/admin/licitacoes/" + id))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(id.toString())));
    }

    @Test
    void buscarPorId_idInexistente_devolve404() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.buscarPorId(id)).thenThrow(new RecursoNaoEncontradoException("Licitacao nao encontrada"));

        mockMvc.perform(get("/api/admin/licitacoes/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void criar_requisicaoValida_devolve201ComLicitacaoCriada() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.criar(any(SalvarLicitacaoRequest.class))).thenReturn(dto(id));

        mockMvc.perform(post("/api/admin/licitacoes")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString(id.toString())));
    }

    @Test
    void criar_campoObrigatorioEmBranco_devolve400ComMensagemDoCampo() throws Exception {
        mockMvc.perform(post("/api/admin/licitacoes")
                        .contentType("application/json")
                        .content("{\"orgao\":\"\",\"objeto\":\"Contratacao de servicos de RH\",\"modalidade\":null,\"numeroProcesso\":null,\"uf\":null,\"valorEstimado\":null,\"dataAberturaProposta\":null,\"dataEncerramentoProposta\":null,\"linkEdital\":null,\"observacoes\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("orgao")));
    }

    @Test
    void atualizarStatus_requisicaoValida_devolve200() throws Exception {
        UUID id = UUID.randomUUID();
        AtualizarStatusLicitacaoRequest request = new AtualizarStatusLicitacaoRequest(Licitacao.StatusLicitacao.GANHOU);
        when(service.atualizarStatus(eq(id), eq(Licitacao.StatusLicitacao.GANHOU))).thenReturn(dto(id));

        mockMvc.perform(patch("/api/admin/licitacoes/" + id + "/status")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(id.toString())));
    }

    @Test
    void criarDocumento_requisicaoValida_devolve201() throws Exception {
        UUID licitacaoId = UUID.randomUUID();
        UUID documentoId = UUID.randomUUID();
        SalvarDocumentoLicitacaoRequest request = new SalvarDocumentoLicitacaoRequest("Certidao negativa");
        DocumentoLicitacaoDto documentoDto = new DocumentoLicitacaoDto(documentoId, licitacaoId, "Certidao negativa", false, null);
        when(service.criarDocumento(eq(licitacaoId), any(SalvarDocumentoLicitacaoRequest.class))).thenReturn(documentoDto);

        mockMvc.perform(post("/api/admin/licitacoes/" + licitacaoId + "/documentos")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString(documentoId.toString())));
    }

    @Test
    void atualizarDocumento_requisicaoValida_devolve200() throws Exception {
        UUID documentoId = UUID.randomUUID();
        AtualizarDocumentoLicitacaoRequest request = new AtualizarDocumentoLicitacaoRequest(true);
        DocumentoLicitacaoDto documentoDto = new DocumentoLicitacaoDto(documentoId, UUID.randomUUID(), "Certidao negativa", true, null);
        when(service.atualizarDocumento(documentoId, true)).thenReturn(documentoDto);

        mockMvc.perform(patch("/api/admin/licitacoes/documentos/" + documentoId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(documentoId.toString())));
    }

    @Test
    void excluirDocumento_idExistente_devolve204() throws Exception {
        UUID documentoId = UUID.randomUUID();

        mockMvc.perform(delete("/api/admin/licitacoes/documentos/" + documentoId))
                .andExpect(status().isNoContent());
    }

    @Test
    void obterConfiguracao_devolve200() throws Exception {
        when(service.obterConfiguracao()).thenReturn(new ConfiguracaoLicitacoesDto(true, "rh, consultoria"));

        mockMvc.perform(get("/api/admin/licitacoes/configuracao"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("rh, consultoria")));
    }

    @Test
    void atualizarConfiguracao_requisicaoValida_devolve200() throws Exception {
        AtualizarConfiguracaoLicitacoesRequest request = new AtualizarConfiguracaoLicitacoesRequest(false, "rh, consultoria");
        when(service.atualizarConfiguracao(false, "rh, consultoria")).thenReturn(new ConfiguracaoLicitacoesDto(false, "rh, consultoria"));

        mockMvc.perform(patch("/api/admin/licitacoes/configuracao")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("rh, consultoria")));
    }
}
