package br.com.mhr.backend.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import br.com.mhr.backend.model.Licitacao;
import br.com.mhr.backend.model.Usuario;
import br.com.mhr.backend.repository.LicitacaoRepository;
import br.com.mhr.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacaoLicitacoesServiceTest {

    @Mock
    private LicitacaoRepository licitacaoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private LogSistemaService logSistemaService;

    @InjectMocks
    private NotificacaoLicitacoesService service;

    @Test
    void semLicitacoesComPrazoProximo_naoEnviaEmail() {
        when(licitacaoRepository.findByDataEncerramentoPropostaBeforeAndStatusNotIn(any(), any()))
                .thenReturn(List.of());

        service.alertarPrazosProximos();

        verify(usuarioRepository, never()).findByAtivoTrueAndPapel(any());
        verify(emailService, never()).enviarAlertaPrazoLicitacao(any(), anyLong(), any());
    }

    @Test
    void comPrazoJaVencido_naoContaComoPendente() {
        Licitacao vencida = new Licitacao();
        vencida.setDataEncerramentoProposta(Instant.now().minus(1, ChronoUnit.DAYS));
        when(licitacaoRepository.findByDataEncerramentoPropostaBeforeAndStatusNotIn(any(), any()))
                .thenReturn(List.of(vencida));

        service.alertarPrazosProximos();

        verify(emailService, never()).enviarAlertaPrazoLicitacao(any(), anyLong(), any());
    }

    @Test
    void comPrazoProximoAindaNaoEnviado_avisaTodoAdminAtivo() {
        Licitacao proxima = new Licitacao();
        proxima.setDataEncerramentoProposta(Instant.now().plus(1, ChronoUnit.DAYS));
        when(licitacaoRepository.findByDataEncerramentoPropostaBeforeAndStatusNotIn(any(), any()))
                .thenReturn(List.of(proxima));

        Usuario admin = new Usuario();
        admin.setEmail("admin@mhr.com.br");
        when(usuarioRepository.findByAtivoTrueAndPapel(Usuario.Papel.ADMIN)).thenReturn(List.of(admin));

        service.alertarPrazosProximos();

        verify(emailService).enviarAlertaPrazoLicitacao("admin@mhr.com.br", 1, proxima.getDataEncerramentoProposta());
    }
}
