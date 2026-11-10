package br.com.mhr.backend.controller.admin;

import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.common.ApiExceptionHandler;
import br.com.mhr.backend.dto.LeadContatoDto;
import br.com.mhr.backend.dto.request.AtualizarClienteLeadRequest;
import br.com.mhr.backend.dto.request.AtualizarStatusLeadRequest;
import br.com.mhr.backend.model.LeadContato;
import br.com.mhr.backend.service.LeadContatoService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LeadContatoAdminControllerTest {

    @Mock
    private LeadContatoService service;
    @Mock
    private LogSistemaService logSistemaService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new LeadContatoAdminController(service))
                .setControllerAdvice(new ApiExceptionHandler(logSistemaService))
                .build();
    }

    private LeadContatoDto dto(UUID id) {
        return new LeadContatoDto(id, "Mariana Costa", "mariana@email.com", "11977776666", "Empresa Y", "Mensagem de contato",
                LeadContato.StatusLead.NOVO, null, false, null, null);
    }

    @Test
    void listarTodos_devolveListaDoService() throws Exception {
        when(service.listarTodos()).thenReturn(List.of(dto(UUID.randomUUID())));

        mockMvc.perform(get("/api/admin/leads-contato"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Mariana Costa")));
    }

    @Test
    void atualizarStatus_requisicaoValida_devolve200ComLeadAtualizado() throws Exception {
        UUID id = UUID.randomUUID();
        AtualizarStatusLeadRequest request = new AtualizarStatusLeadRequest(LeadContato.StatusLead.EM_ANDAMENTO);
        when(service.atualizarStatus(eq(id), any(AtualizarStatusLeadRequest.class))).thenReturn(dto(id));

        mockMvc.perform(patch("/api/admin/leads-contato/" + id + "/status")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(id.toString())));
    }

    @Test
    void atualizarStatus_campoObrigatorioFaltando_devolve400ComMensagemDoCampo() throws Exception {
        mockMvc.perform(patch("/api/admin/leads-contato/" + UUID.randomUUID() + "/status")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("status")));
    }

    @Test
    void atualizarCliente_requisicaoValida_devolve200ComLeadAtualizado() throws Exception {
        UUID id = UUID.randomUUID();
        AtualizarClienteLeadRequest request = new AtualizarClienteLeadRequest(true, UUID.randomUUID());
        when(service.atualizarCliente(eq(id), any(AtualizarClienteLeadRequest.class))).thenReturn(dto(id));

        mockMvc.perform(patch("/api/admin/leads-contato/" + id + "/cliente")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(id.toString())));
    }
}
