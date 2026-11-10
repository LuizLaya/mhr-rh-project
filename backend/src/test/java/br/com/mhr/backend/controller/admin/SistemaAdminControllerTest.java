package br.com.mhr.backend.controller.admin;

import br.com.mhr.backend.common.ApiExceptionHandler;
import br.com.mhr.backend.dto.SistemaInfoDto;
import br.com.mhr.backend.service.LogSistemaService;
import br.com.mhr.backend.service.SistemaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SistemaAdminControllerTest {

    @Mock
    private SistemaService service;
    @Mock
    private LogSistemaService logSistemaService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new SistemaAdminController(service))
                .setControllerAdvice(new ApiExceptionHandler(logSistemaService))
                .build();
    }

    @Test
    void versao_devolveCorpoDoService() throws Exception {
        when(service.obterInfo()).thenReturn(new SistemaInfoDto("1.0.0", null, "25", "4.1.2", "PostgreSQL", "producao"));

        mockMvc.perform(get("/api/admin/sistema/versao"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("producao")));
    }
}
