package br.com.mhr.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class TurnstileServiceTest {

    @Mock
    private LogSistemaService logSistemaService;

    @InjectMocks
    private TurnstileService service;

    @Test
    void verificar_comTokenNulo_devolveFalseSemChamarAApi() {
        assertThat(service.verificar(null)).isFalse();
        verifyNoInteractions(logSistemaService);
    }

    @Test
    void verificar_comTokenEmBranco_devolveFalseSemChamarAApi() {
        assertThat(service.verificar("   ")).isFalse();
        verifyNoInteractions(logSistemaService);
    }
}
