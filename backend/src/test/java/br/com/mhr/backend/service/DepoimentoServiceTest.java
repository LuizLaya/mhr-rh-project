package br.com.mhr.backend.service;

import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.dto.DepoimentoDto;
import br.com.mhr.backend.dto.request.SalvarDepoimentoRequest;
import br.com.mhr.backend.mapper.DepoimentoMapper;
import br.com.mhr.backend.model.Depoimento;
import br.com.mhr.backend.repository.DepoimentoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepoimentoServiceTest {

    @Mock
    private DepoimentoRepository repository;
    @Mock
    private DepoimentoMapper mapper;

    @InjectMocks
    private DepoimentoService service;

    @Test
    void listarPublicados_devolveOsDtosNaOrdemDoRepository() {
        Depoimento depoimento = new Depoimento();
        when(repository.findByPublicadoTrueOrderByOrdemExibicaoAsc()).thenReturn(List.of(depoimento));
        when(mapper.toDto(depoimento)).thenReturn(
                new DepoimentoDto(null, "Autor", null, null, "conteudo", null, 0, true, null));

        List<DepoimentoDto> resultado = service.listarPublicados();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).nomeAutor()).isEqualTo("Autor");
    }

    @Test
    void criar_vinculaOClienteIdQuandoInformado() {
        UUID clienteId = UUID.randomUUID();
        SalvarDepoimentoRequest request = new SalvarDepoimentoRequest("Autor", null, null, "conteudo", null, 0, true, clienteId);
        Depoimento entidade = new Depoimento();
        entidade.setClienteId(clienteId);
        when(mapper.toEntity(request)).thenReturn(entidade);
        when(repository.save(entidade)).thenReturn(entidade);
        when(mapper.toDto(entidade)).thenReturn(
                new DepoimentoDto(null, "Autor", null, null, "conteudo", null, 0, true, clienteId));

        DepoimentoDto resultado = service.criar(request);

        assertThat(resultado.clienteId()).isEqualTo(clienteId);
    }
}
