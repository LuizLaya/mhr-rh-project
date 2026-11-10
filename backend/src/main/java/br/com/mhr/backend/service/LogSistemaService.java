package br.com.mhr.backend.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import br.com.mhr.backend.model.LogSistema;
import br.com.mhr.backend.repository.LogSistemaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Grava eventos na tabela {@code log_sistema} para a tela "Logs" do painel admin, e
 * limpa registros com mais de 7 dias (semanalmente) para a tabela não crescer sem
 * limite - esse log é para diagnóstico recente, não para histórico permanente.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogSistemaService {

    private static final int RETENCAO_DIAS = 7;

    private final LogSistemaRepository repository;

    // ID que agrupa todas as linhas de log de uma mesma execucao de rotina agendada
    // (ver iniciarCorrelacao/encerrarCorrelacao) - thread-local porque cada rotina
    // agendada roda na sua propria thread, e so deve marcar os logs dela mesma.
    private final ThreadLocal<String> correlacaoAtual = new ThreadLocal<>();

    /**
     * Marca, para a thread atual, que todo log registrado a partir daqui pertence à
     * mesma execução (ex.: uma sincronização do PNCP gera uma linha de log por UF,
     * todas com o mesmo id) - chame {@link #encerrarCorrelacao()} num {@code finally}
     * ao fim da rotina, senão a próxima execução nessa thread herdaria o id errado.
     */
    public String iniciarCorrelacao() {
        String correlacaoId = UUID.randomUUID().toString();
        correlacaoAtual.set(correlacaoId);
        return correlacaoId;
    }

    public void encerrarCorrelacao() {
        correlacaoAtual.remove();
    }

    public void info(String categoria, String mensagem, String usuarioEmail) {
        registrar(LogSistema.Nivel.INFO, categoria, mensagem, usuarioEmail, null);
    }

    public void aviso(String categoria, String mensagem, String usuarioEmail) {
        registrar(LogSistema.Nivel.AVISO, categoria, mensagem, usuarioEmail, null);
    }

    /** Mesmo que {@link #aviso(String, String, String)}, mas guardando também o endpoint HTTP envolvido. */
    public void aviso(String categoria, String mensagem, String usuarioEmail, String endpoint) {
        registrar(LogSistema.Nivel.AVISO, categoria, mensagem, usuarioEmail, endpoint);
    }

    public void erro(String categoria, String mensagem, String usuarioEmail) {
        registrar(LogSistema.Nivel.ERRO, categoria, mensagem, usuarioEmail, null);
    }

    /** Mesmo que {@link #erro(String, String, String)}, mas guardando também o endpoint HTTP que originou o erro. */
    public void erro(String categoria, String mensagem, String usuarioEmail, String endpoint) {
        registrar(LogSistema.Nivel.ERRO, categoria, mensagem, usuarioEmail, endpoint);
    }

    private void registrar(LogSistema.Nivel nivel, String categoria, String mensagem, String usuarioEmail, String endpoint) {
        LogSistema entidade = new LogSistema();
        entidade.setNivel(nivel);
        entidade.setCategoria(categoria);
        entidade.setMensagem(mensagem);
        entidade.setUsuarioEmail(usuarioEmail);
        entidade.setEndpoint(endpoint);
        entidade.setOrigem(capturarOrigemDoChamador());
        entidade.setCorrelacaoId(correlacaoAtual.get());
        repository.save(entidade);
    }

    // Descobre "Classe.metodo" de quem chamou info/aviso/erro: pula o frame do proprio
    // getStackTrace e todos os frames internos desta classe (capturarOrigemDoChamador,
    // registrar, info/aviso/erro), e devolve o primeiro frame depois deles - assim
    // nenhum chamador precisa informar isso manualmente, e call sites existentes
    // continuam compilando sem mudanca.
    private String capturarOrigemDoChamador() {
        boolean dentroDesteServico = false;
        for (StackTraceElement frame : Thread.currentThread().getStackTrace()) {
            boolean ehEsteServico = frame.getClassName().equals(LogSistemaService.class.getName());
            if (ehEsteServico) {
                dentroDesteServico = true;
                continue;
            }
            if (dentroDesteServico) {
                String classeSimples = frame.getClassName();
                int ultimoPonto = classeSimples.lastIndexOf('.');
                if (ultimoPonto >= 0) classeSimples = classeSimples.substring(ultimoPonto + 1);
                return classeSimples + "." + frame.getMethodName();
            }
        }
        return null;
    }

    // Toda segunda as 3h: nao precisa de precisao, so de rodar com alguma regularidade.
    @Scheduled(cron = "0 0 3 * * MON")
    public void limparAntigos() {
        Instant limite = Instant.now().minus(RETENCAO_DIAS, ChronoUnit.DAYS);
        long removidos = repository.deleteByCriadoEmBefore(limite);
        if (removidos > 0) {
            log.info("Limpeza semanal do log_sistema removeu {} registro(s) com mais de {} dias", removidos, RETENCAO_DIAS);
        }
    }
}
