package br.com.mhr.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;
    @Mock
    private LogSistemaService logSistemaService;

    @Test
    void enviar_semUsuarioSmtpConfigurado_naoEnviaERegistraAviso() {
        EmailService service = new EmailService(mailSender, "", "", logSistemaService);

        boolean resultado = service.enviarRedefinicaoSenha("destino@mhr.com.br", "http://link");

        assertThat(resultado).isFalse();
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
        verify(logSistemaService).aviso(anyString(), anyString(), org.mockito.ArgumentMatchers.eq("destino@mhr.com.br"));
    }

    @Test
    void enviar_comEnvioBemSucedido_devolveTrueERegistraInfo() {
        EmailService service = new EmailService(mailSender, "resend", "onboarding@resend.dev", logSistemaService);

        boolean resultado = service.enviarConviteAdmin("destino@mhr.com.br", "Admin MHR", "http://link");

        assertThat(resultado).isTrue();
        verify(mailSender).send(any(SimpleMailMessage.class));
        verify(logSistemaService).info(anyString(), anyString(), org.mockito.ArgumentMatchers.eq("destino@mhr.com.br"));
    }

    @Test
    void enviar_quandoProvedorRecusa_naoPropagaExcecaoEDevolveFalse() {
        EmailService service = new EmailService(mailSender, "resend", "onboarding@resend.dev", logSistemaService);
        doThrow(new MailSendException("501 Bad sender address syntax")).when(mailSender).send(any(SimpleMailMessage.class));

        boolean resultado = service.enviarConviteAdmin("destino@exemplo.com", "Admin MHR", "http://link");

        assertThat(resultado).isFalse();
        verify(logSistemaService).erro(anyString(), anyString(), org.mockito.ArgumentMatchers.eq("destino@exemplo.com"));
    }

    @Test
    void remetente_semMailFromConfigurado_caiDeVoltaParaUsuarioSmtp() {
        EmailService service = new EmailService(mailSender, "contato@gmail.com", "", logSistemaService);

        service.enviarRedefinicaoSenha("destino@mhr.com.br", "http://link");

        org.mockito.ArgumentCaptor<SimpleMailMessage> captor = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getFrom()).isEqualTo("contato@gmail.com");
    }
}
