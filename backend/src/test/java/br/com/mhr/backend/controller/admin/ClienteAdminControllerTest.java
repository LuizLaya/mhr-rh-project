package br.com.mhr.backend.controller.admin;

import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.common.ApiExceptionHandler;
import br.com.mhr.backend.dto.ClienteDto;
import br.com.mhr.backend.dto.request.SalvarClienteRequest;
import br.com.mhr.backend.service.ClienteService;
import br.com.mhr.backend.service.LogSistemaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ClienteAdminControllerTest {

    @Mock
    private ClienteService service;
    @Mock
    private LogSistemaService logSistemaService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ClienteAdminController(service))
                .setControllerAdvice(new ApiExceptionHandler(logSistemaService))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    private ClienteDto dto(UUID id) {
        return new ClienteDto(id, "Empresa X", "00.000.000/0001-00", "Joana Silva", "joana@empresax.com", "11999999999", "Observacoes", "http://logo", true, null);
    }

    private SalvarClienteRequest request() {
        return new SalvarClienteRequest("Empresa X", "00.000.000/0001-00", "Joana Silva", "joana@empresax.com", "11999999999", "Observacoes", "http://logo", true);
    }

    @Test
    void listarTodos_devolveListaDoService() throws Exception {
        when(service.listarTodos()).thenReturn(List.of(dto(UUID.randomUUID())));

        mockMvc.perform(get("/api/admin/clientes"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Empresa X")));
    }

    @Test
    void listarPaginado_devolvePaginaDoService() throws Exception {
        ClienteDto dto = dto(UUID.randomUUID());
        when(service.listarPaginado(any(), any())).thenReturn(new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/admin/clientes/paginado").param("busca", "Empresa").param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nomeEmpresa").value("Empresa X"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void criar_requisicaoValida_devolve201ComClienteCriado() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.criar(any(SalvarClienteRequest.class))).thenReturn(dto(id));

        mockMvc.perform(post("/api/admin/clientes")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString(id.toString())));
    }

    @Test
    void criar_campoObrigatorioEmBranco_devolve400ComMensagemDoCampo() throws Exception {
        mockMvc.perform(post("/api/admin/clientes")
                        .contentType("application/json")
                        .content("{\"nomeEmpresa\":\"\",\"cnpj\":null,\"contatoNome\":null,\"contatoEmail\":null,\"contatoTelefone\":null,\"observacoes\":null,\"logoUrl\":null,\"exibirNoSite\":true}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("nomeEmpresa")));
    }

    @Test
    void atualizar_requisicaoValida_devolve200ComClienteAtualizado() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.atualizar(eq(id), any(SalvarClienteRequest.class))).thenReturn(dto(id));

        mockMvc.perform(put("/api/admin/clientes/" + id)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(id.toString())));
    }

    @Test
    void excluir_idExistente_devolve204EChamaService() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/admin/clientes/" + id))
                .andExpect(status().isNoContent());

        verify(service).excluir(id);
    }
}
