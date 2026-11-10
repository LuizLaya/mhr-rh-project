package br.com.mhr.backend.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

/**
 * Um evento registrado para a tela "Logs" do painel admin (login, envio de e-mail,
 * erro inesperado). Mapeia a tabela {@code log_sistema}.
 *
 * <p>Não é uma trilha de auditoria completa (não registra cada ação de CRUD) — é um
 * diagnóstico rápido de "o que deu certo/errado e quando", por isso a tabela é
 * limpa automaticamente toda semana (ver {@link LogSistemaService#limparAntigos()}).
 */
@Entity
@Table(name = "log_sistema")
@Getter
@Setter
@NoArgsConstructor
public class LogSistema {

    @Id
    @UuidGenerator
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Nivel nivel;

    @Column(nullable = false, length = 50)
    private String categoria;

    @Column(nullable = false, columnDefinition = "text")
    private String mensagem;

    // E-mail relacionado ao evento (quem logou, para quem o e-mail era) - nem todo
    // evento tem um, por isso fica nulavel (ex.: um erro de sistema sem usuario associado).
    @Column(name = "usuario_email", length = 180)
    private String usuarioEmail;

    // Classe + metodo de onde o evento foi registrado (ex.: "PncpSyncService.sincronizarUf"),
    // capturado automaticamente pelo LogSistemaService - nao exige que cada chamador informe.
    @Column(length = 255)
    private String origem;

    // Metodo + caminho da requisicao HTTP que originou o erro (ex.: "POST /api/admin/licitacoes/sincronizar"),
    // so preenchido para erros que vieram de uma requisicao (ver ApiExceptionHandler); nulo para
    // eventos de rotinas agendadas, que nao tem requisicao associada.
    @Column(length = 255)
    private String endpoint;

    // Agrupa todas as linhas de uma mesma execucao de rotina agendada (ex.: uma
    // sincronizacao do PNCP gera varias linhas - uma por UF - todas com o mesmo
    // correlacaoId). Nulo fora de uma rotina agendada (ver LogSistemaService).
    @Column(name = "correlacao_id", length = 36)
    private String correlacaoId;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private Instant criadoEm;

    public enum Nivel {
        INFO, AVISO, ERRO
    }
}
