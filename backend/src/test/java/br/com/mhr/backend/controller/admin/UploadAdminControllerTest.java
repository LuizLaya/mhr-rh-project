package br.com.mhr.backend.controller.admin;

import br.com.mhr.backend.common.ApiExceptionHandler;
import br.com.mhr.backend.dto.UploadDto;
import br.com.mhr.backend.service.LogSistemaService;
import br.com.mhr.backend.service.UploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UploadAdminControllerTest {

    @Mock
    private UploadService uploadService;
    @Mock
    private LogSistemaService logSistemaService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UploadAdminController(uploadService))
                .setControllerAdvice(new ApiExceptionHandler(logSistemaService))
                .build();
    }

    @Test
    void enviar_arquivoValido_devolve201ComUrlDoService() throws Exception {
        MockMultipartFile arquivo = new MockMultipartFile("arquivo", "foto.png", "image/png", "conteudo".getBytes());
        when(uploadService.salvar(any(), anyString())).thenReturn(new UploadDto("http://localhost:8080/uploads/abc.png"));

        mockMvc.perform(multipart("/api/admin/uploads").file(arquivo))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("http://localhost:8080/uploads/abc.png")));
    }

    @Test
    void enviar_tipoNaoAceito_devolve400ComMensagemDoService() throws Exception {
        MockMultipartFile arquivo = new MockMultipartFile("arquivo", "documento.pdf", "application/pdf", "conteudo".getBytes());
        when(uploadService.salvar(any(), anyString()))
                .thenThrow(new IllegalArgumentException("Tipo de arquivo nao aceito (use JPEG, PNG, WEBP ou GIF)"));

        mockMvc.perform(multipart("/api/admin/uploads").file(arquivo))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Tipo de arquivo nao aceito")));
    }
}
