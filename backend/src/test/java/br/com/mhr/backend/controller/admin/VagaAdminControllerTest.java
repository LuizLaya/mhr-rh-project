package br.com.mhr.backend.controller.admin;

import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.common.ApiExceptionHandler;
import br.com.mhr.backend.dto.VagaDto;
import br.com.mhr.backend.dto.request.SalvarVagaRequest;
import br.com.mhr.backend.model.Vaga;
import br.com.mhr.backend.service.LogSistemaService;
import br.com.mhr.backend.service.VagaService;
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
class VagaAdminControllerTest {

    @Mock
    private VagaService service;
    @Mock
    private LogSistemaService logSistemaService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new VagaAdminController(service))
                .setControllerAdvice(new ApiExceptionHandler(logSistemaService))
                .build();
    }

    private VagaDto dto(UUID id) {
        return new VagaDto(id, "Analista de RH", "analista-de-rh", "Descricao", "Sao Paulo", Vaga.ModeloTrabalho.HIBRIDO, Vaga.StatusVaga.ABERTA, null);
    }

    private SalvarVagaRequest request() {
        return new SalvarVagaRequest("Analista de RH", "analista-de-rh", "Descricao", "Sao Paulo", Vaga.ModeloTrabalho.HIBRIDO, Vaga.StatusVaga.ABERTA);
    }

    @Test
    void listarTodas_devolveListaDoService() throws Exception {
        when(service.listarTodas()).thenReturn(List.of(dto(UUID.randomUUID())));

        mockMvc.perform(get("/api/admin/vagas"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Analista de RH")));
    }

    @Test
    void criar_requisicaoValida_devolve201ComVagaCriada() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.criar(any(SalvarVagaRequest.class))).thenReturn(dto(id));

        mockMvc.perform(post("/api/admin/vagas")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString(id.toString())));
    }

    @Test
    void criar_campoObrigatorioEmBranco_devolve400ComMensagemDoCampo() throws Exception {
        mockMvc.perform(post("/api/admin/vagas")
                        .contentType("application/json")
                        .content("{\"titulo\":\"\",\"slug\":\"analista-de-rh\",\"descricao\":\"Descricao\",\"cidade\":\"Sao Paulo\",\"modeloTrabalho\":\"HIBRIDO\",\"status\":\"ABERTA\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("titulo")));
    }

    @Test
    void atualizar_requisicaoValida_devolve200ComVagaAtualizada() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.atualizar(eq(id), any(SalvarVagaRequest.class))).thenReturn(dto(id));

        mockMvc.perform(put("/api/admin/vagas/" + id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(id.toString())));
    }

    @Test
    void excluir_idExistente_devolve204EChamaService() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/admin/vagas/" + id))
                .andExpect(status().isNoContent());

        verify(service).excluir(id);
    }
}
