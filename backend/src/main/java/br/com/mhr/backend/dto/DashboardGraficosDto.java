package br.com.mhr.backend.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import br.com.mhr.backend.service.DashboardService;

/**
 * Dados para os gráficos do dashboard (ver {@link DashboardService#graficos()}).
 * Separado do {@link DashboardResumoDto} (números simples, consultados a cada carga
 * da tela) porque aqui o cálculo é mais pesado (agrupamento em memória).
 */
public record DashboardGraficosDto(
        List<PontoDiario> leadsPorDia,
        List<PontoDiario> candidaturasPorDia,
        Map<String, Long> leadsPorStatus,
        Map<String, Long> vagasPorStatus,
        Map<String, Long> conteudoPorTipo
) {
    public record PontoDiario(LocalDate data, long quantidade) {
    }
}
