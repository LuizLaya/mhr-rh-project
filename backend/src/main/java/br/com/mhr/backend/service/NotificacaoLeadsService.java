package br.com.mhr.backend.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import br.com.mhr.backend.model.LeadContato;
import br.com.mhr.backend.model.Usuario;
import br.com.mhr.backend.repository.LeadContatoRepository;
import br.com.mhr.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Avisa por e-mail todo ADMIN ativo quando há mensagens de contato ({@link LeadContato})
 * com status {@code NOVO} há mais de 1 dia sem resposta. Roda uma vez por dia; enquanto
 * a mensagem continuar sem resposta, o aviso se repete no dia seguinte (é um lembrete,
 * não uma notificação única).
 */
@Service
@RequiredArgsConstructor
public class NotificacaoLeadsService {

    private static final long HORAS_SEM_RESPOSTA = 24;

    private final LeadContatoRepository leadContatoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final LogSistemaService logSistemaService;

    // Todo dia as 9h: horário comercial, para quem for checar o e-mail de manhã já ver o aviso.
    @Scheduled(cron = "0 0 9 * * *")
    public void alertarMensagensPendentes() {
        logSistemaService.iniciarCorrelacao();
        try {
            executar();
        } finally {
            logSistemaService.encerrarCorrelacao();
        }
    }

    private void executar() {
        Instant limite = Instant.now().minus(HORAS_SEM_RESPOSTA, ChronoUnit.HOURS);
        List<LeadContato> pendentes =
                leadContatoRepository.findByStatusAndCriadoEmBeforeOrderByCriadoEmAsc(LeadContato.StatusLead.NOVO, limite);

        if (pendentes.isEmpty()) {
            return;
        }

        Instant maisAntiga = pendentes.get(0).getCriadoEm();
        List<Usuario> admins = usuarioRepository.findByAtivoTrueAndPapel(Usuario.Papel.ADMIN);

        for (Usuario admin : admins) {
            emailService.enviarAlertaMensagensPendentes(admin.getEmail(), pendentes.size(), maisAntiga);
        }
    }
}
