package br.com.mhr.backend.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import br.com.mhr.backend.dto.DashboardGraficosDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    private static final ZoneId FUSO_BRASILIA = ZoneId.of("America/Sao_Paulo");

    @Mock
    private br.com.mhr.backend.repository.VagaRepository vagaRepository;
    @Mock
    private br.com.mhr.backend.repository.CandidaturaRepository candidaturaRepository;
    @Mock
    private br.com.mhr.backend.repository.ServicoRepository servicoRepository;
    @Mock
    private br.com.mhr.backend.repository.DepoimentoRepository depoimentoRepository;
    @Mock
    private br.com.mhr.backend.repository.PostBlogRepository postBlogRepository;
    @Mock
    private br.com.mhr.backend.repository.LeadContatoRepository leadContatoRepository;
    @Mock
    private br.com.mhr.backend.repository.UsuarioRepository usuarioRepository;
    @Mock
    private br.com.mhr.backend.repository.ClienteRepository clienteRepository;

    @InjectMocks
    private DashboardService service;

    @Test
    void agruparPorDia_agrupaInstantesDoMesmoDiaESomaAQuantidade() {
        LocalDate hoje = LocalDate.now(FUSO_BRASILIA);
        Instant manha = hoje.atStartOfDay(FUSO_BRASILIA).plusHours(8).toInstant();
        Instant noite = hoje.atStartOfDay(FUSO_BRASILIA).plusHours(20).toInstant();
        Instant ontem = hoje.minusDays(1).atStartOfDay(FUSO_BRASILIA).plusHours(10).toInstant();

        List<DashboardGraficosDto.PontoDiario> resultado = service.agruparPorDia(List.of(manha, noite, ontem));

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).data()).isEqualTo(hoje.minusDays(1));
        assertThat(resultado.get(0).quantidade()).isEqualTo(1);
        assertThat(resultado.get(1).data()).isEqualTo(hoje);
        assertThat(resultado.get(1).quantidade()).isEqualTo(2);
    }

    @Test
    void agruparPorDia_semDatas_devolveListaVazia() {
        List<DashboardGraficosDto.PontoDiario> resultado = service.agruparPorDia(List.of());

        assertThat(resultado).isEmpty();
    }
}
