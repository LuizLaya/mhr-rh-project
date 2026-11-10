package br.com.mhr.backend.controller.admin;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.common.ApiExceptionHandler;
import br.com.mhr.backend.dto.ServicoDto;
import br.com.mhr.backend.dto.request.SalvarServicoRequest;
import br.com.mhr.backend.service.LogSistemaService;
import br.com.mhr.backend.service.ServicoService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ServicoAdminControllerTest {

    @Mock
    private ServicoService service;
    @Mock
    private LogSistemaService logSistemaService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ServicoAdminController(service))
                .setControllerAdvice(new ApiExceptionHandler(logSistemaService))
                .build();
    }

    private ServicoDto dto(UUID id) {
        return new ServicoDto(id, "Consultoria em RH", "consultoria-rh", "Resumo", "Conteudo", "icone", 0, true, Instant.now());
    }

    private SalvarServicoRequest request() {
        return new SalvarServicoRequest("Consultoria em RH", "consultoria-rh", "Resumo", "Conteudo", "icone", 0, true);
    }

    @Test
    void listarTodos_devolveListaDoService() throws Exception {
        when(service.listarTodos()).thenReturn(List.of(dto(UUID.randomUUID())));

        mockMvc.perform(get("/api/admin/servicos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Consultoria em RH")));
    }

    @Test
    void criar_requisicaoValida_devolve201ComServicoCriado() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.criar(any(SalvarServicoRequest.class))).thenReturn(dto(id));

        mockMvc.perform(post("/api/admin/servicos")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString(id.toString())));
    }

    @Test
    void criar_campoObrigatorioEmBranco_devolve400ComMensagemDoCampo() throws Exception {
        mockMvc.perform(post("/api/admin/servicos")
                        .contentType("application/json")
                        .content("{\"titulo\":\"\",\"slug\":\"consultoria-rh\",\"resumo\":\"Resumo\",\"conteudo\":\"Conteudo\",\"icone\":\"icone\",\"ordemExibicao\":0,\"publicado\":true}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("titulo")));
    }

    @Test
    void atualizar_requisicaoValida_devolve200ComServicoAtualizado() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.atualizar(eq(id), any(SalvarServicoRequest.class))).thenReturn(dto(id));

        mockMvc.perform(put("/api/admin/servicos/" + id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(id.toString())));
    }

    @Test
    void excluir_idExistente_devolve204EChamaService() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/admin/servicos/" + id))
                .andExpect(status().isNoContent());

        verify(service).excluir(id);
    }
}
