package br.com.mhr.backend.controller.admin;

import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.common.ApiExceptionHandler;
import br.com.mhr.backend.dto.MembroEquipeDto;
import br.com.mhr.backend.dto.request.SalvarMembroEquipeRequest;
import br.com.mhr.backend.service.LogSistemaService;
import br.com.mhr.backend.service.MembroEquipeService;
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
class EquipeAdminControllerTest {

    @Mock
    private MembroEquipeService service;
    @Mock
    private LogSistemaService logSistemaService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new EquipeAdminController(service))
                .setControllerAdvice(new ApiExceptionHandler(logSistemaService))
                .build();
    }

    private MembroEquipeDto dto(UUID id) {
        return new MembroEquipeDto(id, "Carlos Souza", "Gerente", "http://foto", "Bio", 0, true, null);
    }

    private SalvarMembroEquipeRequest request() {
        return new SalvarMembroEquipeRequest("Carlos Souza", "Gerente", "http://foto", "Bio", 0, true);
    }

    @Test
    void listarTodos_devolveListaDoService() throws Exception {
        when(service.listarTodos()).thenReturn(List.of(dto(UUID.randomUUID())));

        mockMvc.perform(get("/api/admin/equipe"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Carlos Souza")));
    }

    @Test
    void criar_requisicaoValida_devolve201ComMembroCriado() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.criar(any(SalvarMembroEquipeRequest.class))).thenReturn(dto(id));

        mockMvc.perform(post("/api/admin/equipe")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString(id.toString())));
    }

    @Test
    void criar_campoObrigatorioEmBranco_devolve400ComMensagemDoCampo() throws Exception {
        mockMvc.perform(post("/api/admin/equipe")
                        .contentType("application/json")
                        .content("{\"nome\":\"\",\"cargo\":\"Gerente\",\"fotoUrl\":null,\"bio\":\"Bio\",\"ordemExibicao\":0,\"publicado\":true}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("nome")));
    }

    @Test
    void atualizar_requisicaoValida_devolve200ComMembroAtualizado() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.atualizar(eq(id), any(SalvarMembroEquipeRequest.class))).thenReturn(dto(id));

        mockMvc.perform(put("/api/admin/equipe/" + id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(id.toString())));
    }

    @Test
    void excluir_idExistente_devolve204EChamaService() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/admin/equipe/" + id))
                .andExpect(status().isNoContent());

        verify(service).excluir(id);
    }
}
