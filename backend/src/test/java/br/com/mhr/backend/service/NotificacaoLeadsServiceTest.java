package br.com.mhr.backend.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import br.com.mhr.backend.model.LeadContato;
import br.com.mhr.backend.model.Usuario;
import br.com.mhr.backend.repository.LeadContatoRepository;
import br.com.mhr.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacaoLeadsServiceTest {

    @Mock
    private LeadContatoRepository leadContatoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private LogSistemaService logSistemaService;

    @InjectMocks
    private NotificacaoLeadsService service;

    @Test
    void semMensagensPendentes_naoEnviaNenhumEmail() {
        when(leadContatoRepository.findByStatusAndCriadoEmBeforeOrderByCriadoEmAsc(any(), any()))
                .thenReturn(List.of());

        service.alertarMensagensPendentes();

        verify(usuarioRepository, never()).findByAtivoTrueAndPapel(any());
        verify(emailService, never()).enviarAlertaMensagensPendentes(any(), anyLong(), any());
    }

    @Test
    void comMensagensPendentes_avisaTodoAdminAtivo() {
        LeadContato antiga = new LeadContato();
        antiga.setCriadoEm(Instant.now().minus(3, ChronoUnit.DAYS));
        LeadContato maisRecente = new LeadContato();
        maisRecente.setCriadoEm(Instant.now().minus(2, ChronoUnit.DAYS));
        when(leadContatoRepository.findByStatusAndCriadoEmBeforeOrderByCriadoEmAsc(eq(LeadContato.StatusLead.NOVO), any()))
                .thenReturn(List.of(antiga, maisRecente));

        Usuario admin1 = new Usuario();
        admin1.setEmail("admin1@mhr.com.br");
        Usuario admin2 = new Usuario();
        admin2.setEmail("admin2@mhr.com.br");
        when(usuarioRepository.findByAtivoTrueAndPapel(Usuario.Papel.ADMIN)).thenReturn(List.of(admin1, admin2));

        service.alertarMensagensPendentes();

        verify(emailService).enviarAlertaMensagensPendentes("admin1@mhr.com.br", 2, antiga.getCriadoEm());
        verify(emailService).enviarAlertaMensagensPendentes("admin2@mhr.com.br", 2, antiga.getCriadoEm());
    }
}
