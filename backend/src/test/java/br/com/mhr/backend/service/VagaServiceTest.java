package br.com.mhr.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.mhr.backend.common.RecursoNaoEncontradoException;
import br.com.mhr.backend.dto.VagaDto;
import br.com.mhr.backend.mapper.VagaMapper;
import br.com.mhr.backend.model.Vaga;
import br.com.mhr.backend.repository.VagaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VagaServiceTest {

    @Mock
    private VagaRepository repository;
    @Mock
    private VagaMapper mapper;

    @InjectMocks
    private VagaService service;

    @Test
    void listarAbertas_filtraApenasVagasComStatusAberta() {
        Vaga aberta = new Vaga();
        aberta.setStatus(Vaga.StatusVaga.ABERTA);
        Vaga encerrada = new Vaga();
        encerrada.setStatus(Vaga.StatusVaga.ENCERRADA);
        when(repository.findAll()).thenReturn(List.of(aberta, encerrada));
        when(mapper.toDto(aberta)).thenReturn(new VagaDto(null, null, null, null, null, null, Vaga.StatusVaga.ABERTA, null));

        List<VagaDto> resultado = service.listarAbertas();

        assertThat(resultado).hasSize(1);
    }

    @Test
    void excluir_comIdInexistente_lancaRecursoNaoEncontradoException() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.excluir(id)).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void criar_comVagaAberta_marcaPublicadoEm() {
        Vaga vaga = new Vaga();
        vaga.setStatus(Vaga.StatusVaga.ABERTA);
        when(mapper.toEntity(any())).thenReturn(vaga);
        when(repository.save(vaga)).thenReturn(vaga);
        when(mapper.toDto(vaga)).thenReturn(new VagaDto(null, null, null, null, null, null, Vaga.StatusVaga.ABERTA, null));

        service.criar(new br.com.mhr.backend.dto.request.SalvarVagaRequest(
                "Titulo", "slug", "descricao", "Brasilia", Vaga.ModeloTrabalho.REMOTO, Vaga.StatusVaga.ABERTA));

        assertThat(vaga.getPublicadoEm()).isNotNull();
    }
}
