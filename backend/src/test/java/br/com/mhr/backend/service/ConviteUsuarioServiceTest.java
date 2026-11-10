package br.com.mhr.backend.service;

import java.util.UUID;

import br.com.mhr.backend.model.Usuario;
import br.com.mhr.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConviteUsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private TokenAcessoService tokenAcessoService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;

    private ConviteUsuarioService service;
    private Usuario convidadoPor;

    @BeforeEach
    void setUp() {
        service = new ConviteUsuarioService(usuarioRepository, tokenAcessoService, passwordEncoder, emailService);
        ReflectionTestUtils.setField(service, "frontendAdminUrl", "http://localhost:5173");

        convidadoPor = new Usuario();
        convidadoPor.setId(UUID.randomUUID());
        convidadoPor.setNome("Administrador MHR");
    }

    @Test
    void convidar_comEmailJaCadastrado_lancaIllegalArgumentExceptionSemCriarNada() {
        when(usuarioRepository.existsByEmail("existente@mhr.com.br")).thenReturn(true);

        assertThatThrownBy(() -> service.convidar("Alguem", "existente@mhr.com.br", Usuario.Papel.EDITOR, convidadoPor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ja existe um usuario");

        verifyNoInteractions(tokenAcessoService, emailService);
    }

    @Test
    void convidar_criaUsuarioInativoParaLoginComSenhaNaoDefinida() {
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("senha-hash-inutilizavel");
        when(tokenAcessoService.gerarToken(any())).thenReturn("token-bruto");
        when(emailService.enviarConviteAdmin(anyString(), anyString(), anyString())).thenReturn(true);

        service.convidar("Junior", "junior@mhr.com.br", Usuario.Papel.ADMIN, convidadoPor);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario novo = captor.getValue();

        assertThat(novo.getEmail()).isEqualTo("junior@mhr.com.br");
        assertThat(novo.getPapel()).isEqualTo(Usuario.Papel.ADMIN);
        assertThat(novo.isSenhaDefinida()).isFalse();
        assertThat(novo.isAtivo()).isTrue();
        assertThat(novo.getConvidadoPor()).isEqualTo(convidadoPor.getId());
        assertThat(novo.getSenhaHash()).isEqualTo("senha-hash-inutilizavel");
    }

    @Test
    void convidar_devolveEmailEnviadoIgualAoRetornoDoEmailService() {
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(tokenAcessoService.gerarToken(any())).thenReturn("token-bruto");
        when(emailService.enviarConviteAdmin(anyString(), anyString(), anyString())).thenReturn(false);

        ConviteUsuarioService.ResultadoConvite resultado =
                service.convidar("Alguem", "alguem@mhr.com.br", Usuario.Papel.EDITOR, convidadoPor);

        assertThat(resultado.emailEnviado()).isFalse();
    }
}
