package br.com.mhr.backend.controller.admin;

import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.common.ApiExceptionHandler;
import br.com.mhr.backend.dto.CandidaturaDto;
import br.com.mhr.backend.dto.request.AtualizarStatusCandidaturaRequest;
import br.com.mhr.backend.model.Candidatura;
import br.com.mhr.backend.service.CandidaturaService;
import br.com.mhr.backend.service.LogSistemaService;
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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CandidaturaAdminControllerTest {

    @Mock
    private CandidaturaService service;
    @Mock
    private LogSistemaService logSistemaService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CandidaturaAdminController(service))
                .setControllerAdvice(new ApiExceptionHandler(logSistemaService))
                .build();
    }

    private CandidaturaDto dto(UUID id, UUID vagaId) {
        return new CandidaturaDto(id, vagaId, "Pedro Alves", "pedro@email.com", "11988887777", "http://curriculo", true, Candidatura.StatusCandidatura.NOVA, null);
    }

    @Test
    void listar_semVagaId_devolveListaDoService() throws Exception {
        when(service.listar(isNull())).thenReturn(List.of(dto(UUID.randomUUID(), UUID.randomUUID())));

        mockMvc.perform(get("/api/admin/candidaturas"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Pedro Alves")));

        verify(service).listar(isNull());
    }

    @Test
    void listar_comVagaId_passaUuidParaOService() throws Exception {
        UUID vagaId = UUID.randomUUID();
        when(service.listar(eq(vagaId))).thenReturn(List.of(dto(UUID.randomUUID(), vagaId)));

        mockMvc.perform(get("/api/admin/candidaturas").param("vagaId", vagaId.toString()))
                .andExpect(status().isOk());

        verify(service).listar(vagaId);
    }

    @Test
    void atualizarStatus_requisicaoValida_devolve200ComCandidaturaAtualizada() throws Exception {
        UUID id = UUID.randomUUID();
        AtualizarStatusCandidaturaRequest request = new AtualizarStatusCandidaturaRequest(Candidatura.StatusCandidatura.APROVADO);
        when(service.atualizarStatus(eq(id), any(AtualizarStatusCandidaturaRequest.class))).thenReturn(dto(id, UUID.randomUUID()));

        mockMvc.perform(patch("/api/admin/candidaturas/" + id + "/status")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(id.toString())));
    }

    @Test
    void atualizarStatus_camposObrigatoriosFaltando_devolve400ComMensagemDoCampo() throws Exception {
        mockMvc.perform(patch("/api/admin/candidaturas/" + UUID.randomUUID() + "/status")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("status")));
    }
}
