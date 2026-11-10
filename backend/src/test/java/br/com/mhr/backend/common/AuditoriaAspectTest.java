package br.com.mhr.backend.common;

import br.com.mhr.backend.service.LogSistemaService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditoriaAspectTest {

    @Mock
    private LogSistemaService logSistemaService;
    @Mock
    private JoinPoint joinPoint;
    @Mock
    private Signature signature;

    @InjectMocks
    private AuditoriaAspect aspect;

    @AfterEach
    void limparContextoDeSeguranca() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registrarAlteracao_logaCategoriaAuditoriaComOEmailAutenticadoEOMetodoChamado() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin@mhr.com.br", null, java.util.List.of()));
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringType()).thenReturn(br.com.mhr.backend.controller.admin.VagaAdminController.class);
        when(signature.getName()).thenReturn("criar");

        aspect.registrarAlteracao(joinPoint);

        verify(logSistemaService).info(eq("AUDITORIA"), contains("VagaAdminController.criar"), eq("admin@mhr.com.br"));
    }

    @Test
    void registrarAlteracao_semAutenticacao_logaComUsuarioNulo() {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringType()).thenReturn(br.com.mhr.backend.controller.admin.VagaAdminController.class);
        when(signature.getName()).thenReturn("excluir");

        aspect.registrarAlteracao(joinPoint);

        verify(logSistemaService).info(eq("AUDITORIA"), contains("VagaAdminController.excluir"), eq(null));
    }
}
