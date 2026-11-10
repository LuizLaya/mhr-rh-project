package br.com.mhr.backend.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

import br.com.mhr.backend.model.Licitacao;
import br.com.mhr.backend.model.Usuario;
import br.com.mhr.backend.repository.LicitacaoRepository;
import br.com.mhr.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Avisa por e-mail todo ADMIN ativo quando há {@link Licitacao} com prazo de
 * envio de proposta a menos de 2 dias e que ainda não chegou a um status
 * terminal (nem proposta enviada, nem resultado, nem descartada). Mesmo
 * padrão de {@link NotificacaoLeadsService}: roda uma vez por dia e repete o
 * aviso enquanto a licitação continuar pendente.
 */
@Service
@RequiredArgsConstructor
public class NotificacaoLicitacoesService {

    private static final long DIAS_DE_ANTECEDENCIA = 2;

    private static final List<Licitacao.StatusLicitacao> STATUS_SEM_ALERTA = List.of(
            Licitacao.StatusLicitacao.PROPOSTA_ENVIADA,
            Licitacao.StatusLicitacao.GANHOU,
            Licitacao.StatusLicitacao.PERDEU,
            Licitacao.StatusLicitacao.DESCARTADA
    );

    private final LicitacaoRepository licitacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final LogSistemaService logSistemaService;

    // Todo dia as 9h, junto com o alerta de mensagens pendentes.
    @Scheduled(cron = "0 0 9 * * *")
    public void alertarPrazosProximos() {
        logSistemaService.iniciarCorrelacao();
        try {
            executar();
        } finally {
            logSistemaService.encerrarCorrelacao();
        }
    }

    private void executar() {
        Instant limite = Instant.now().plus(DIAS_DE_ANTECEDENCIA, ChronoUnit.DAYS);
        List<Licitacao> pendentes = licitacaoRepository
                .findByDataEncerramentoPropostaBeforeAndStatusNotIn(limite, STATUS_SEM_ALERTA)
                .stream()
                .filter(l -> l.getDataEncerramentoProposta() != null && l.getDataEncerramentoProposta().isAfter(Instant.now()))
                .toList();

        if (pendentes.isEmpty()) {
            return;
        }

        Instant prazoMaisProximo = pendentes.stream()
                .map(Licitacao::getDataEncerramentoProposta)
                .min(Comparator.naturalOrder())
                .orElseThrow();

        List<Usuario> admins = usuarioRepository.findByAtivoTrueAndPapel(Usuario.Papel.ADMIN);
        for (Usuario admin : admins) {
            emailService.enviarAlertaPrazoLicitacao(admin.getEmail(), pendentes.size(), prazoMaisProximo);
        }
    }
}
