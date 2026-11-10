package br.com.mhr.backend.service;

import java.util.Optional;
import java.util.UUID;

import br.com.mhr.backend.model.Usuario;
import br.com.mhr.backend.repository.UsuarioRepository;
import br.com.mhr.backend.mapper.UsuarioMapper;
import br.com.mhr.backend.dto.UsuarioDto;
import org.junit.jupiter.api.BeforeEach;
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
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;
    @Mock
    private UsuarioMapper mapper;
    @Mock
    private ConviteUsuarioService conviteUsuarioService;

    @InjectMocks
    private UsuarioService service;

    private Usuario admin;

    @BeforeEach
    void setUp() {
        admin = new Usuario();
        admin.setId(UUID.randomUUID());
        admin.setEmail("admin@mhr.com.br");
        admin.setAtivo(true);
    }

    @Test
    void atualizarAcesso_tentandoDesativarAPropriaConta_lancaIllegalArgumentException() {
        when(repository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(repository.findByEmail("admin@mhr.com.br")).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.atualizarAcesso(admin.getId(), false, "admin@mhr.com.br"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nao pode desativar sua propria conta");
    }

    @Test
    void atualizarAcesso_desativandoOutraConta_funcionaNormalmente() {
        Usuario outro = new Usuario();
        outro.setId(UUID.randomUUID());
        outro.setAtivo(true);

        when(repository.findById(outro.getId())).thenReturn(Optional.of(outro));
        when(repository.findByEmail("admin@mhr.com.br")).thenReturn(Optional.of(admin));
        when(repository.save(outro)).thenReturn(outro);
        when(mapper.toDto(outro)).thenReturn(new UsuarioDto(outro.getId(), null, null, null, false, true, null));

        UsuarioDto resultado = service.atualizarAcesso(outro.getId(), false, "admin@mhr.com.br");

        assertThat(outro.isAtivo()).isFalse();
        assertThat(resultado.ativo()).isFalse();
    }

    @Test
    void atualizarAcesso_reativandoAPropriaConta_naoLancaExcecao() {
        when(repository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(repository.findByEmail("admin@mhr.com.br")).thenReturn(Optional.of(admin));
        when(repository.save(admin)).thenReturn(admin);
        when(mapper.toDto(any())).thenReturn(new UsuarioDto(admin.getId(), null, null, null, true, true, null));

        service.atualizarAcesso(admin.getId(), true, "admin@mhr.com.br");

        assertThat(admin.isAtivo()).isTrue();
    }
}
