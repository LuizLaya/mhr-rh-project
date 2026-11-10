package br.com.mhr.backend.controller.admin;

import java.util.List;
import java.util.Map;

import br.com.mhr.backend.common.ApiExceptionHandler;
import br.com.mhr.backend.dto.DashboardGraficosDto;
import br.com.mhr.backend.dto.DashboardResumoDto;
import br.com.mhr.backend.service.DashboardService;
import br.com.mhr.backend.service.LogSistemaService;
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
class DashboardControllerTest {

    @Mock
    private DashboardService service;
    @Mock
    private LogSistemaService logSistemaService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new DashboardController(service))
                .setControllerAdvice(new ApiExceptionHandler(logSistemaService))
                .build();
    }

    @Test
    void resumo_devolveCorpoDoService() throws Exception {
        when(service.resumo()).thenReturn(new DashboardResumoDto(5, 10, 3, 2, 4, 1, 6, 2, 7));

        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"vagasAbertas\":5")));
    }

    @Test
    void graficos_devolveCorpoDoService() throws Exception {
        DashboardGraficosDto graficos = new DashboardGraficosDto(
                List.of(), List.of(), Map.of("NOVO", 1L), Map.of("ABERTA", 1L), Map.of("SERVICO", 1L));
        when(service.graficos()).thenReturn(graficos);

        mockMvc.perform(get("/api/admin/dashboard/graficos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("leadsPorStatus")));
    }
}
