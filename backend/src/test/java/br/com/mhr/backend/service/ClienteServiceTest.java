package br.com.mhr.backend.service;

import java.util.Optional;
import java.util.UUID;

import br.com.mhr.backend.common.RecursoNaoEncontradoException;
import br.com.mhr.backend.dto.ClienteDto;
import br.com.mhr.backend.dto.request.SalvarClienteRequest;
import br.com.mhr.backend.mapper.ClienteMapper;
import br.com.mhr.backend.model.Cliente;
import br.com.mhr.backend.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository repository;
    @Mock
    private ClienteMapper mapper;

    @InjectMocks
    private ClienteService service;

    @Test
    void criar_salvaEDevolveODto() {
        SalvarClienteRequest request = new SalvarClienteRequest("Empresa Teste", null, null, null, null, null, null, false);
        Cliente entidade = new Cliente();
        when(mapper.toEntity(request)).thenReturn(entidade);
        when(repository.save(entidade)).thenReturn(entidade);
        when(mapper.toDto(entidade)).thenReturn(new ClienteDto(null, "Empresa Teste", null, null, null, null, null, null, false, null));

        ClienteDto resultado = service.criar(request);

        assertThat(resultado.nomeEmpresa()).isEqualTo("Empresa Teste");
    }

    @Test
    void atualizar_comIdInexistente_lancaRecursoNaoEncontradoException() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar(id, new SalvarClienteRequest("X", null, null, null, null, null, null, false)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void listarPaginado_devolveAPaginaConvertidaParaDto() {
        Cliente cliente = new Cliente();
        cliente.setNomeEmpresa("Empresa Paginada");
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 20);
        org.springframework.data.domain.Page<Cliente> pagina =
                new org.springframework.data.domain.PageImpl<>(java.util.List.of(cliente), pageable, 1);
        when(repository.buscarPaginado("empresa", pageable)).thenReturn(pagina);
        when(mapper.toDto(cliente)).thenReturn(
                new ClienteDto(null, "Empresa Paginada", null, null, null, null, null, null, false, null));

        var resultado = service.listarPaginado("empresa", pageable);

        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent().get(0).nomeEmpresa()).isEqualTo("Empresa Paginada");
    }

    @Test
    void listarDestaques_devolveSoNomeELogoDosMarcadosParaExibir() {
        Cliente cliente = new Cliente();
        cliente.setNomeEmpresa("Empresa Destaque");
        cliente.setLogoUrl("http://exemplo.com/logo.png");
        cliente.setExibirNoSite(true);
        when(repository.findByExibirNoSiteTrueOrderByNomeEmpresa()).thenReturn(java.util.List.of(cliente));

        var resultado = service.listarDestaques();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).nomeEmpresa()).isEqualTo("Empresa Destaque");
        assertThat(resultado.get(0).logoUrl()).isEqualTo("http://exemplo.com/logo.png");
    }
}
