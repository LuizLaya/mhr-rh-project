package br.com.mhr.backend.controller.admin;

import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.common.ApiExceptionHandler;
import br.com.mhr.backend.dto.LogSistemaDto;
import br.com.mhr.backend.mapper.LogSistemaMapper;
import br.com.mhr.backend.model.LogSistema;
import br.com.mhr.backend.repository.LogSistemaRepository;
import br.com.mhr.backend.service.LogSistemaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LogSistemaAdminControllerTest {

    @Mock
    private LogSistemaRepository repository;
    @Mock
    private LogSistemaMapper mapper;
    @Mock
    private LogSistemaService logSistemaService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new LogSistemaAdminController(repository, mapper))
                .setControllerAdvice(new ApiExceptionHandler(logSistemaService))
                .build();
    }

    @Test
    void listarRecentes_devolveListaMapeadaDoRepositorio() throws Exception {
        LogSistema entidade = new LogSistema();
        LogSistemaDto dto = new LogSistemaDto(UUID.randomUUID(), LogSistema.Nivel.INFO, "SISTEMA", "Mensagem de teste", null, null, null, null, null);
        when(repository.findTop200ByOrderByCriadoEmDesc()).thenReturn(List.of(entidade));
        when(mapper.toDto(entidade)).thenReturn(dto);

        mockMvc.perform(get("/api/admin/logs"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Mensagem de teste")));
    }

    @Test
    void excluir_idExistente_devolve204() throws Exception {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);

        mockMvc.perform(delete("/api/admin/logs/" + id))
                .andExpect(status().isNoContent());

        verify(repository).deleteById(id);
    }

    @Test
    void excluir_idInexistente_devolve404ENaoExclui() throws Exception {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(false);

        mockMvc.perform(delete("/api/admin/logs/" + id))
                .andExpect(status().isNotFound());

        verify(repository, never()).deleteById(any());
    }

    @Test
    void limparTodos_devolve204EChamaDeleteAll() throws Exception {
        mockMvc.perform(delete("/api/admin/logs"))
                .andExpect(status().isNoContent());

        verify(repository).deleteAll();
    }
}
