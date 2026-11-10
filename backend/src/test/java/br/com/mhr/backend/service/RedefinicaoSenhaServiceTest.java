package br.com.mhr.backend.service;

import java.util.Optional;
import java.util.UUID;

import br.com.mhr.backend.model.Usuario;
import br.com.mhr.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedefinicaoSenhaServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private TokenAcessoService tokenAcessoService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;

    private RedefinicaoSenhaService service;

    @BeforeEach
    void setUp() {
        service = new RedefinicaoSenhaService(usuarioRepository, tokenAcessoService, passwordEncoder, emailService);
        ReflectionTestUtils.setField(service, "frontendAdminUrl", "http://localhost:5173");
    }

    @Test
    void solicitar_comEmailInexistente_naoFazNadaENaoLancaExcecao() {
        when(usuarioRepository.findByEmail("fantasma@mhr.com.br")).thenReturn(Optional.empty());

        service.solicitar("fantasma@mhr.com.br");

        verify(emailService, never()).enviarRedefinicaoSenha(anyString(), anyString());
    }

    @Test
    void solicitar_comUsuarioDesativado_naoEnviaEmail() {
        Usuario desativado = new Usuario();
        desativado.setEmail("inativo@mhr.com.br");
        desativado.setAtivo(false);
        when(usuarioRepository.findByEmail("inativo@mhr.com.br")).thenReturn(Optional.of(desativado));

        service.solicitar("inativo@mhr.com.br");

        verify(emailService, never()).enviarRedefinicaoSenha(anyString(), anyString());
    }

    @Test
    void solicitar_comUsuarioAtivo_geraTokenEEnviaEmail() {
        UUID id = UUID.randomUUID();
        Usuario ativo = new Usuario();
        ativo.setId(id);
        ativo.setEmail("ativo@mhr.com.br");
        ativo.setAtivo(true);
        when(usuarioRepository.findByEmail("ativo@mhr.com.br")).thenReturn(Optional.of(ativo));
        when(tokenAcessoService.gerarToken(id)).thenReturn("token-bruto");

        service.solicitar("ativo@mhr.com.br");

        verify(emailService).enviarRedefinicaoSenha("ativo@mhr.com.br", "http://localhost:5173/redefinir-senha?token=token-bruto");
    }

    @Test
    void redefinir_comTokenValido_atualizaSenhaEMarcaSenhaDefinida() {
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setAtivo(true);
        usuario.setSenhaDefinida(false);
        when(tokenAcessoService.consumir("token-bruto")).thenReturn(usuarioId);
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("nova-senha")).thenReturn("hash-nova-senha");

        service.redefinir("token-bruto", "nova-senha");

        assertThat(usuario.getSenhaHash()).isEqualTo("hash-nova-senha");
        assertThat(usuario.isSenhaDefinida()).isTrue();
    }

    @Test
    void redefinir_comUsuarioDesativado_lancaIllegalArgumentException() {
        UUID usuarioId = UUID.randomUUID();
        Usuario desativado = new Usuario();
        desativado.setId(usuarioId);
        desativado.setAtivo(false);
        when(tokenAcessoService.consumir("token-bruto")).thenReturn(usuarioId);
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(desativado));

        assertThatThrownBy(() -> service.redefinir("token-bruto", "nova-senha"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
