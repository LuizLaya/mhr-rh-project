package br.com.mhr.backend.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import br.com.mhr.backend.model.ConfiguracaoLicitacoes;
import br.com.mhr.backend.model.Licitacao;
import br.com.mhr.backend.repository.ConfiguracaoLicitacoesRepository;
import br.com.mhr.backend.repository.LicitacaoRepository;
import br.com.mhr.backend.service.pncp.PncpContratacaoDto;
import br.com.mhr.backend.service.pncp.PncpPaginaDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Busca automaticamente, na API pública de consulta do PNCP (Portal Nacional de
 * Contratações Públicas), licitações com proposta aberta que combinem com a
 * área de atuação da MHR, e cria uma {@link Licitacao} para cada uma que ainda
 * não foi importada.
 *
 * <p><b>Limitação real da API</b>: o PNCP não tem busca por palavra-chave —
 * só filtra por data, UF, município, órgão e modalidade. O "combinar com a
 * área da MHR" é feito aqui, procurando a lista de palavras-chave (editável
 * pelo admin na tela de Licitações, guardada em {@link ConfiguracaoLicitacoes})
 * dentro do texto livre de {@code objetoCompra} de cada resultado trazido por
 * UF. Isso pode gerar falso-negativo (edital relevante com objeto descrito de
 * um jeito que não bate a lista) — a lista de palavras é pensada pra ser
 * ajustada com o uso real, não é definitiva.</p>
 *
 * <p>Só roda (agendado ou sob demanda) quando {@link ConfiguracaoLicitacoes}
 * está com a busca ligada — ver {@code LicitacaoAdminController}, onde o admin
 * liga/desliga.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PncpSyncService {

    private static final DateTimeFormatter FORMATO_DATA_PNCP = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final ZoneId FUSO_BRASILIA = ZoneId.of("America/Sao_Paulo");
    private static final int TAMANHO_PAGINA = 50;
    private static final int MAX_PAGINAS_POR_UF = 20;

    private final RestClient pncpRestClient;
    private final LicitacaoRepository licitacaoRepository;
    private final ConfiguracaoLicitacoesRepository configuracaoRepository;
    private final LogSistemaService logSistemaService;

    @Value("${app.licitacoes.ufs}")
    private String ufsConfiguradas;

    // Todo dia as 7h, antes do horario comercial.
    @Scheduled(cron = "0 0 7 * * *")
    public void sincronizarAgendado() {
        ConfiguracaoLicitacoes configuracao = configuracaoRepository.findById(ConfiguracaoLicitacoes.ID_UNICO).orElse(null);
        if (configuracao == null || !configuracao.isBuscaPncpAtiva()) {
            return;
        }
        logSistemaService.iniciarCorrelacao();
        try {
            sincronizar(configuracao.getPalavrasChave());
        } finally {
            logSistemaService.encerrarCorrelacao();
        }
    }

    /** @throws IllegalArgumentException se a busca automática estiver desligada na configuração. */
    public ResultadoSincronizacao sincronizarAgora() {
        ConfiguracaoLicitacoes configuracao = configuracaoRepository.findById(ConfiguracaoLicitacoes.ID_UNICO).orElse(null);
        if (configuracao == null || !configuracao.isBuscaPncpAtiva()) {
            throw new IllegalArgumentException(
                    "A busca automatica no PNCP esta desativada. Ative em Licitacoes > Configuracao antes de sincronizar.");
        }
        logSistemaService.iniciarCorrelacao();
        try {
            return sincronizar(configuracao.getPalavrasChave());
        } finally {
            logSistemaService.encerrarCorrelacao();
        }
    }

    private ResultadoSincronizacao sincronizar(String palavrasChaveConfiguradas) {
        List<String> ufs = List.of(ufsConfiguradas.split(","));
        List<String> palavrasChave = List.of(palavrasChaveConfiguradas.split(","));
        String dataFinal = LocalDate.now(FUSO_BRASILIA).plusDays(90).format(FORMATO_DATA_PNCP);

        int analisadas = 0;
        int importadas = 0;

        for (String ufBruta : ufs) {
            String uf = ufBruta.trim().toUpperCase(Locale.ROOT);
            if (uf.isBlank()) {
                continue;
            }
            try {
                ResultadoUf resultado = sincronizarUf(uf, dataFinal, palavrasChave);
                analisadas += resultado.analisadas();
                importadas += resultado.importadas();
            } catch (Exception e) {
                log.error("Falha ao sincronizar licitacoes do PNCP para UF {}", uf, e);
                logSistemaService.erro("LICITACAO_SYNC", "Falha ao consultar PNCP para UF " + uf + ": " + e.getMessage(), null);
            }
        }

        String mensagem = "Sincronizacao PNCP concluida: " + analisadas + " contratacoes analisadas, " +
                importadas + " licitacoes novas importadas.";
        log.info(mensagem);
        logSistemaService.info("LICITACAO_SYNC", mensagem, null);
        return new ResultadoSincronizacao(analisadas, importadas);
    }

    private ResultadoUf sincronizarUf(String uf, String dataFinal, List<String> palavrasChave) {
        int analisadas = 0;
        int importadas = 0;

        for (int pagina = 1; pagina <= MAX_PAGINAS_POR_UF; pagina++) {
            int paginaAtual = pagina;
            PncpPaginaDto resposta = pncpRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/contratacoes/proposta")
                            .queryParam("dataFinal", dataFinal)
                            .queryParam("uf", uf)
                            .queryParam("pagina", paginaAtual)
                            .queryParam("tamanhoPagina", TAMANHO_PAGINA)
                            .build())
                    .retrieve()
                    .body(PncpPaginaDto.class);

            if (resposta == null || resposta.data() == null || resposta.data().isEmpty()) {
                break;
            }

            analisadas += resposta.data().size();
            for (PncpContratacaoDto contratacao : resposta.data()) {
                if (combinaComPalavraChave(contratacao.objetoCompra(), palavrasChave)
                        && !licitacaoRepository.existsByNumeroControlePncp(contratacao.numeroControlePNCP())) {
                    licitacaoRepository.save(paraLicitacao(contratacao, uf));
                    importadas++;
                }
            }

            if (pagina >= resposta.totalPaginas()) {
                break;
            }
        }

        return new ResultadoUf(analisadas, importadas);
    }

    boolean combinaComPalavraChave(String objetoCompra, List<String> palavrasChave) {
        if (objetoCompra == null || objetoCompra.isBlank()) {
            return false;
        }
        String objetoEmMinusculas = objetoCompra.toLowerCase(Locale.ROOT);
        return palavrasChave.stream()
                .map(p -> p.trim().toLowerCase(Locale.ROOT))
                .filter(p -> !p.isBlank())
                .anyMatch(objetoEmMinusculas::contains);
    }

    private Licitacao paraLicitacao(PncpContratacaoDto contratacao, String ufConsultada) {
        Licitacao licitacao = new Licitacao();
        licitacao.setNumeroControlePncp(contratacao.numeroControlePNCP());
        licitacao.setOrigem(Licitacao.Origem.PNCP_AUTOMATICO);
        licitacao.setOrgao(contratacao.orgaoEntidade() != null ? contratacao.orgaoEntidade().razaoSocial() : "Não informado");
        licitacao.setObjeto(contratacao.objetoCompra());
        licitacao.setModalidade(contratacao.modalidadeNome());
        licitacao.setNumeroProcesso(contratacao.processo());
        licitacao.setUf(contratacao.unidadeOrgao() != null && contratacao.unidadeOrgao().ufSigla() != null
                ? contratacao.unidadeOrgao().ufSigla() : ufConsultada);
        licitacao.setValorEstimado(contratacao.valorTotalEstimado());
        licitacao.setDataAberturaProposta(paraInstant(contratacao.dataAberturaProposta()));
        licitacao.setDataEncerramentoProposta(paraInstant(contratacao.dataEncerramentoProposta()));
        licitacao.setLinkEdital(contratacao.linkSistemaOrigem() != null ? contratacao.linkSistemaOrigem() : contratacao.linkProcessoEletronico());
        licitacao.setStatus(Licitacao.StatusLicitacao.IDENTIFICADA);
        return licitacao;
    }

    // O PNCP devolve data/hora sem timezone (horario local do Brasil).
    private Instant paraInstant(java.time.LocalDateTime localDateTime) {
        return localDateTime == null ? null : localDateTime.atZone(FUSO_BRASILIA).toInstant();
    }

    private record ResultadoUf(int analisadas, int importadas) {
    }

    public record ResultadoSincronizacao(int analisadas, int importadas) {
    }
}
