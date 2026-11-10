package br.com.mhr.backend.common;

import br.com.mhr.backend.controller.LeadContatoController;
import br.com.mhr.backend.service.LeadContatoService;
import br.com.mhr.backend.service.LogSistemaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testa a camada HTTP real (Spring MVC + validação @Valid + ApiExceptionHandler),
 * sem subir o contexto Spring inteiro nem o filtro de segurança
 * ({@code MockMvcBuilders.standaloneSetup}) - mais rápido que um
 * {@code @WebMvcTest} completo, mas exercita exatamente a mesma resolução de
 * exceção que acontece em produção.
 *
 * <p>Existe porque um teste manual ao vivo achou um bug real: toda falha de
 * {@code @Valid} (ex.: campo obrigatório em branco) virava 500 "erro interno do
 * servidor" em vez de 400 com a mensagem do campo, porque não havia handler
 * específico para {@link org.springframework.web.bind.MethodArgumentNotValidException}
 * e ela caía no catch-all genérico.</p>
 */
@ExtendWith(MockitoExtension.class)
class ApiExceptionHandlerTest {

    @Mock
    private LeadContatoService leadContatoService;
    @Mock
    private LogSistemaService logSistemaService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new LeadContatoController(leadContatoService))
                .setControllerAdvice(new ApiExceptionHandler(logSistemaService))
                .build();
    }

    @Test
    void corpoComCampoObrigatorioEmBranco_devolve400ComAMensagemDoCampo() throws Exception {
        mockMvc.perform(post("/api/leads-contato")
                        .contentType("application/json")
                        .content("{\"nome\":\"\",\"email\":\"invalido\",\"turnstileToken\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("turnstileToken")));
    }
}
