package br.com.mhr.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.mhr.backend.common.RecursoNaoEncontradoException;
import br.com.mhr.backend.dto.MembroEquipeDto;
import br.com.mhr.backend.dto.request.SalvarMembroEquipeRequest;
import br.com.mhr.backend.mapper.MembroEquipeMapper;
import br.com.mhr.backend.model.MembroEquipe;
import br.com.mhr.backend.repository.MembroEquipeRepository;
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
class MembroEquipeServiceTest {

    @Mock
    private MembroEquipeRepository repository;
    @Mock
    private MembroEquipeMapper mapper;

    @InjectMocks
    private MembroEquipeService service;

    @Test
    void criar_salvaEDevolveDto() {
        MembroEquipe entidade = new MembroEquipe();
        SalvarMembroEquipeRequest request = new SalvarMembroEquipeRequest("Fulano", "Cargo", null, null, 0, true);
        when(mapper.toEntity(request)).thenReturn(entidade);
        when(repository.save(entidade)).thenReturn(entidade);
        when(mapper.toDto(entidade)).thenReturn(new MembroEquipeDto(null, "Fulano", "Cargo", null, null, 0, true, null));

        MembroEquipeDto resultado = service.criar(request);

        assertThat(resultado.nome()).isEqualTo("Fulano");
    }

    @Test
    void listarPublicados_devolveSoOsPublicadosOrdenados() {
        when(repository.findByPublicadoTrueOrderByOrdemExibicaoAsc()).thenReturn(List.of(new MembroEquipe()));

        service.listarPublicados();

        verify(repository).findByPublicadoTrueOrderByOrdemExibicaoAsc();
    }

    @Test
    void atualizar_comIdInexistente_lancaRecursoNaoEncontradoException() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar(id, new SalvarMembroEquipeRequest("X", null, null, null, 0, true)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void excluir_removeAEntidadeEncontrada() {
        UUID id = UUID.randomUUID();
        MembroEquipe entidade = new MembroEquipe();
        when(repository.findById(id)).thenReturn(Optional.of(entidade));

        service.excluir(id);

        verify(repository).delete(entidade);
    }
}
