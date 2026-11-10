package br.com.mhr.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.mhr.backend.common.RecursoNaoEncontradoException;
import br.com.mhr.backend.dto.FaqDto;
import br.com.mhr.backend.dto.request.SalvarFaqRequest;
import br.com.mhr.backend.mapper.FaqMapper;
import br.com.mhr.backend.model.Faq;
import br.com.mhr.backend.repository.FaqRepository;
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
class FaqServiceTest {

    @Mock
    private FaqRepository repository;
    @Mock
    private FaqMapper mapper;

    @InjectMocks
    private FaqService service;

    @Test
    void criar_salvaEDevolveDto() {
        Faq entidade = new Faq();
        SalvarFaqRequest request = new SalvarFaqRequest("Pergunta?", "Resposta.", 0, true);
        when(mapper.toEntity(request)).thenReturn(entidade);
        when(repository.save(entidade)).thenReturn(entidade);
        when(mapper.toDto(entidade)).thenReturn(new FaqDto(null, "Pergunta?", "Resposta.", 0, true, null));

        FaqDto resultado = service.criar(request);

        assertThat(resultado.pergunta()).isEqualTo("Pergunta?");
    }

    @Test
    void listarPublicados_devolveSoOsPublicadosOrdenados() {
        when(repository.findByPublicadoTrueOrderByOrdemExibicaoAsc()).thenReturn(List.of(new Faq()));

        service.listarPublicados();

        verify(repository).findByPublicadoTrueOrderByOrdemExibicaoAsc();
    }

    @Test
    void atualizar_comIdInexistente_lancaRecursoNaoEncontradoException() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar(id, new SalvarFaqRequest("X", "Y", 0, true)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void excluir_removeAEntidadeEncontrada() {
        UUID id = UUID.randomUUID();
        Faq entidade = new Faq();
        when(repository.findById(id)).thenReturn(Optional.of(entidade));

        service.excluir(id);

        verify(repository).delete(entidade);
    }
}
