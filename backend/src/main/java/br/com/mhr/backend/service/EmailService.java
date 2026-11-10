package br.com.mhr.backend.service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envio de e-mails transacionais via SMTP (configurado com {@code MAIL_USERNAME}/
 * {@code MAIL_PASSWORD}). Sem essas variáveis, não há como enviar e-mail de verdade —
 * o link é só registrado no log, para não travar o fluxo em desenvolvimento local.
 *
 * <p>O usuário de autenticação SMTP ({@code spring.mail.username}) nem sempre é um
 * endereço de e-mail válido para o cabeçalho "From" (ex.: provedores como o Resend usam
 * um usuário fixo tipo {@code resend}, não um e-mail) — por isso o remetente exibido é
 * configurado separadamente em {@code app.mail.remetente} ({@code MAIL_FROM}), caindo de
 * volta para {@code spring.mail.username} quando não informado (caso do Gmail, onde os
 * dois coincidem).</p>
 *
 * <p>Provedores gratuitos sem domínio verificado (ex.: Resend em modo sandbox) só
 * entregam e-mail para o próprio dono da conta — enviar para qualquer outro destinatário
 * é recusado pelo servidor SMTP. Para esse cenário não travar o fluxo (o convite/link
 * precisa continuar existindo mesmo que o e-mail não saia), uma falha no envio real cai
 * de volta para o mesmo log usado quando não há credenciais configuradas, em vez de
 * propagar a exceção para quem chamou. O chamador recebe {@code false} nesse caso e
 * decide o que fazer (ex.: avisar o admin que o e-mail não saiu de verdade).</p>
 *
 * <p>Todo envio, sucesso ou falha, também vira um registro em {@code log_sistema}
 * (via {@link LogSistemaService}) - é o que alimenta a tela "Logs" do painel.</p>
 */
@Slf4j
@Service
public class EmailService {

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of("America/Sao_Paulo"));

    private final JavaMailSender mailSender;
    private final String usuarioSmtp;
    private final String remetente;
    private final LogSistemaService logSistemaService;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username:}") String usuarioSmtp,
            @Value("${app.mail.remetente:}") String remetente,
            LogSistemaService logSistemaService) {
        this.mailSender = mailSender;
        this.usuarioSmtp = usuarioSmtp;
        this.remetente = remetente.isBlank() ? usuarioSmtp : remetente;
        this.logSistemaService = logSistemaService;
    }

    /** @return {@code true} se o e-mail realmente saiu pelo SMTP configurado. */
    public boolean enviarRedefinicaoSenha(String destinatario, String link) {
        return enviar(
                "REDEFINICAO_SENHA",
                destinatario,
                "Redefinição de senha - Painel MHR",
                "Recebemos um pedido para redefinir sua senha do painel administrativo da MHR.\n\n" +
                        "Clique no link abaixo para escolher uma nova senha (válido por 30 minutos):\n" + link +
                        "\n\nSe você não pediu isso, pode ignorar este e-mail."
        );
    }

    /** @return {@code true} se o e-mail realmente saiu pelo SMTP configurado. */
    public boolean enviarConviteAdmin(String destinatario, String nomeConvidadoPor, String link) {
        return enviar(
                "CONVITE",
                destinatario,
                "Você foi convidado para o painel administrativo da MHR",
                nomeConvidadoPor + " convidou você para acessar o painel administrativo da MHR.\n\n" +
                        "Clique no link abaixo para criar sua senha e ativar o acesso (válido por 30 minutos):\n" + link +
                        "\n\nSe você não esperava este convite, pode ignorar este e-mail."
        );
    }

    /** @return {@code true} se o e-mail realmente saiu pelo SMTP configurado. */
    public boolean enviarAlertaMensagensPendentes(String destinatario, long quantidade, Instant maisAntigaDesde) {
        String plural = quantidade == 1 ? "mensagem" : "mensagens";
        return enviar(
                "ALERTA_LEADS",
                destinatario,
                "Painel MHR: " + quantidade + " " + plural + " de contato aguardando resposta",
                "Há " + quantidade + " " + plural + " de contato com status \"Novo\" no painel administrativo " +
                        "há mais de 1 dia sem resposta. A mais antiga chegou em " + FORMATO_DATA.format(maisAntigaDesde) +
                        ".\n\nAcesse a aba Mensagens no painel para responder."
        );
    }

    /** @return {@code true} se o e-mail realmente saiu pelo SMTP configurado. */
    public boolean enviarAlertaPrazoLicitacao(String destinatario, long quantidade, Instant prazoMaisProximo) {
        String plural = quantidade == 1 ? "licitação" : "licitações";
        return enviar(
                "ALERTA_LICITACAO",
                destinatario,
                "Painel MHR: " + quantidade + " " + plural + " com prazo perto de vencer",
                "Há " + quantidade + " " + plural + " acompanhadas no painel com prazo de envio de proposta " +
                        "em menos de 2 dias, ainda sem proposta enviada. O prazo mais próximo vence em " +
                        FORMATO_DATA.format(prazoMaisProximo) + ".\n\nAcesse a aba Licitações no painel para conferir."
        );
    }

    private boolean enviar(String categoriaLog, String destinatario, String assunto, String corpo) {
        if (usuarioSmtp == null || usuarioSmtp.isBlank()) {
            log.warn("MAIL_USERNAME nao configurado - nenhum e-mail foi enviado para {}. Assunto: '{}'. Corpo:\n{}",
                    destinatario, assunto, corpo);
            logSistemaService.aviso(categoriaLog, "MAIL_USERNAME nao configurado - e-mail nao enviado. Assunto: " + assunto, destinatario);
            return false;
        }

        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(destinatario);
        mensagem.setSubject(assunto);
        mensagem.setText(corpo);
        try {
            mailSender.send(mensagem);
            logSistemaService.info(categoriaLog, "E-mail enviado com sucesso. Assunto: " + assunto, destinatario);
            return true;
        } catch (MailException e) {
            log.error("Falha ao enviar e-mail de verdade para {} (provedor recusou ou falhou) - " +
                    "o link continua valido, repasse manualmente. Assunto: '{}'. Corpo:\n{}",
                    destinatario, assunto, corpo, e);
            logSistemaService.erro(categoriaLog, "Falha ao enviar e-mail: " + e.getMessage() + ". Assunto: " + assunto, destinatario);
            return false;
        }
    }
}
