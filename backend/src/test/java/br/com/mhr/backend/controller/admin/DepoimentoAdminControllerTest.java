package br.com.mhr.backend.controller.admin;

import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.common.ApiExceptionHandler;
import br.com.mhr.backend.dto.DepoimentoDto;
import br.com.mhr.backend.dto.request.SalvarDepoimentoRequest;
import br.com.mhr.backend.service.DepoimentoService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DepoimentoAdminControllerTest {

    @Mock
    private DepoimentoService service;
    @Mock
    private LogSistemaService logSistemaService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new DepoimentoAdminController(service))
                .setControllerAdvice(new ApiExceptionHandler(logSistemaService))
                .build();
    }

    private DepoimentoDto dto(UUID id) {
        return new DepoimentoDto(id, "Joana Silva", "Diretora", "Empresa X", "Excelente trabalho", "http://foto", 0, true, null);
    }

    private SalvarDepoimentoRequest request() {
        return new SalvarDepoimentoRequest("Joana Silva", "Diretora", "Empresa X", "Excelente trabalho", "http://foto", 0, true, null);
    }

    @Test
    void listarTodos_devolveListaDoService() throws Exception {
        when(service.listarTodos()).thenReturn(List.of(dto(UUID.randomUUID())));

        mockMvc.perform(get("/api/admin/depoimentos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Joana Silva")));
    }

    @Test
    void criar_requisicaoValida_devolve201ComDepoimentoCriado() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.criar(any(SalvarDepoimentoRequest.class))).thenReturn(dto(id));

        mockMvc.perform(post("/api/admin/depoimentos")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString(id.toString())));
    }

    @Test
    void criar_campoObrigatorioEmBranco_devolve400ComMensagemDoCampo() throws Exception {
        mockMvc.perform(post("/api/admin/depoimentos")
                        .contentType("application/json")
                        .content("{\"nomeAutor\":\"\",\"cargoAutor\":\"Diretora\",\"nomeEmpresa\":\"Empresa X\",\"conteudo\":\"Excelente\",\"urlFoto\":null,\"ordemExibicao\":0,\"publicado\":true,\"clienteId\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("nomeAutor")));
    }

    @Test
    void atualizar_requisicaoValida_devolve200ComDepoimentoAtualizado() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.atualizar(eq(id), any(SalvarDepoimentoRequest.class))).thenReturn(dto(id));

        mockMvc.perform(put("/api/admin/depoimentos/" + id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(id.toString())));
    }

    @Test
    void excluir_idExistente_devolve204EChamaService() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/admin/depoimentos/" + id))
                .andExpect(status().isNoContent());

        verify(service).excluir(id);
    }
}
