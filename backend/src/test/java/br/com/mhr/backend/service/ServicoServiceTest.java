package br.com.mhr.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.mhr.backend.common.RecursoNaoEncontradoException;
import br.com.mhr.backend.dto.ServicoDto;
import br.com.mhr.backend.dto.request.SalvarServicoRequest;
import br.com.mhr.backend.mapper.ServicoMapper;
import br.com.mhr.backend.model.Servico;
import br.com.mhr.backend.repository.ServicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    @Mock
    private ServicoRepository repository;
    @Mock
    private ServicoMapper mapper;

    @InjectMocks
    private ServicoService service;

    @Test
    void criar_salvaEDevolveDto() {
        Servico entidade = new Servico();
        SalvarServicoRequest request = new SalvarServicoRequest("Titulo", "slug", "resumo", "conteudo", null, 0, true);
        when(mapper.toEntity(request)).thenReturn(entidade);
        when(repository.save(entidade)).thenReturn(entidade);
        when(mapper.toDto(entidade)).thenReturn(new ServicoDto(null, "Titulo", "slug", "resumo", "conteudo", null, 0, true, null));

        ServicoDto resultado = service.criar(request);

        assertThat(resultado.titulo()).isEqualTo("Titulo");
    }

    @Test
    void buscarPorSlug_comSlugInexistente_lancaRecursoNaoEncontradoException() {
        when(repository.findBySlugAndPublicadoTrue("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorSlug("inexistente")).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void excluir_removeAEntidadeEncontrada() {
        UUID id = UUID.randomUUID();
        Servico entidade = new Servico();
        when(repository.findById(id)).thenReturn(Optional.of(entidade));

        service.excluir(id);

        verify(repository).delete(entidade);
    }
}
