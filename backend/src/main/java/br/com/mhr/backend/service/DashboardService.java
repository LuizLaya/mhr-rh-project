package br.com.mhr.backend.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import br.com.mhr.backend.dto.DashboardGraficosDto;
import br.com.mhr.backend.dto.DashboardResumoDto;
import br.com.mhr.backend.model.Candidatura;
import br.com.mhr.backend.model.LeadContato;
import br.com.mhr.backend.model.Vaga;
import br.com.mhr.backend.repository.CandidaturaRepository;
import br.com.mhr.backend.repository.ClienteRepository;
import br.com.mhr.backend.repository.DepoimentoRepository;
import br.com.mhr.backend.repository.LeadContatoRepository;
import br.com.mhr.backend.repository.PostBlogRepository;
import br.com.mhr.backend.repository.ServicoRepository;
import br.com.mhr.backend.repository.UsuarioRepository;
import br.com.mhr.backend.repository.VagaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Números e dados de gráfico agregados para a tela inicial do painel admin — uma
 * consulta só, em vez do front-end buscar cada lista inteira e contar na mão.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int DIAS_GRAFICO = 30;
    private static final ZoneId FUSO_BRASILIA = ZoneId.of("America/Sao_Paulo");

    private final VagaRepository vagaRepository;
    private final CandidaturaRepository candidaturaRepository;
    private final ServicoRepository servicoRepository;
    private final DepoimentoRepository depoimentoRepository;
    private final PostBlogRepository postBlogRepository;
    private final LeadContatoRepository leadContatoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;

    public DashboardResumoDto resumo() {
        return new DashboardResumoDto(
                vagaRepository.countByStatus(Vaga.StatusVaga.ABERTA),
                candidaturaRepository.count(),
                servicoRepository.countByPublicadoTrue(),
                depoimentoRepository.countByPublicadoTrue(),
                postBlogRepository.countByPublicadoTrue(),
                leadContatoRepository.countByStatus(LeadContato.StatusLead.NOVO),
                leadContatoRepository.count(),
                usuarioRepository.countByAtivoTrue(),
                clienteRepository.count()
        );
    }

    public DashboardGraficosDto graficos() {
        Instant limite = Instant.now().minus(DIAS_GRAFICO, ChronoUnit.DAYS);

        List<DashboardGraficosDto.PontoDiario> leadsPorDia = agruparPorDia(
                leadContatoRepository.findByCriadoEmAfter(limite).stream().map(LeadContato::getCriadoEm).toList());
        List<DashboardGraficosDto.PontoDiario> candidaturasPorDia = agruparPorDia(
                candidaturaRepository.findByCriadoEmAfter(limite).stream().map(Candidatura::getCriadoEm).toList());

        Map<String, Long> leadsPorStatus = Map.of(
                "NOVO", leadContatoRepository.countByStatus(LeadContato.StatusLead.NOVO),
                "EM_ANDAMENTO", leadContatoRepository.countByStatus(LeadContato.StatusLead.EM_ANDAMENTO),
                "CONCLUIDO", leadContatoRepository.countByStatus(LeadContato.StatusLead.CONCLUIDO)
        );

        Map<String, Long> vagasPorStatus = Map.of(
                "ABERTA", vagaRepository.countByStatus(Vaga.StatusVaga.ABERTA),
                "ENCERRADA", vagaRepository.countByStatus(Vaga.StatusVaga.ENCERRADA)
        );

        Map<String, Long> conteudoPorTipo = Map.of(
                "Vagas", vagaRepository.count(),
                "Serviços", servicoRepository.count(),
                "Depoimentos", depoimentoRepository.count(),
                "Posts do blog", postBlogRepository.count()
        );

        return new DashboardGraficosDto(leadsPorDia, candidaturasPorDia, leadsPorStatus, vagasPorStatus, conteudoPorTipo);
    }

    // Agrupamento em memoria (nao em SQL) de proposito: o volume de leads/candidaturas
    // da MHR e pequeno, e isso evita uma query dependente de funcao especifica do
    // Postgres so para um grafico do dashboard.
    List<DashboardGraficosDto.PontoDiario> agruparPorDia(List<Instant> datas) {
        Map<LocalDate, Long> porDia = datas.stream()
                .map(instant -> instant.atZone(FUSO_BRASILIA).toLocalDate())
                .collect(Collectors.groupingBy(data -> data, Collectors.counting()));

        return porDia.entrySet().stream()
                .map(entry -> new DashboardGraficosDto.PontoDiario(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(DashboardGraficosDto.PontoDiario::data))
                .toList();
    }
}
