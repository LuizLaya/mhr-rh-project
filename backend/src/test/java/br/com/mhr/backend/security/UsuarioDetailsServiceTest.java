package br.com.mhr.backend.security;

import java.util.Optional;

import br.com.mhr.backend.model.Usuario;
import br.com.mhr.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioDetailsService service;

    @Test
    void loadUserByUsername_comUsuarioAtivo_devolveUserDetailsHabilitado() {
        Usuario usuario = new Usuario();
        usuario.setEmail("admin@mhr.com.br");
        usuario.setSenhaHash("hash-da-senha");
        usuario.setPapel(Usuario.Papel.ADMIN);
        usuario.setAtivo(true);
        when(usuarioRepository.findByEmail("admin@mhr.com.br")).thenReturn(Optional.of(usuario));

        UserDetails userDetails = service.loadUserByUsername("admin@mhr.com.br");

        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.getPassword()).isEqualTo("hash-da-senha");
        assertThat(userDetails.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_comUsuarioDesativado_devolveUserDetailsDesabilitado() {
        Usuario usuario = new Usuario();
        usuario.setEmail("editor@mhr.com.br");
        usuario.setSenhaHash("hash");
        usuario.setPapel(Usuario.Papel.EDITOR);
        usuario.setAtivo(false);
        when(usuarioRepository.findByEmail("editor@mhr.com.br")).thenReturn(Optional.of(usuario));

        UserDetails userDetails = service.loadUserByUsername("editor@mhr.com.br");

        assertThat(userDetails.isEnabled()).isFalse();
    }

    @Test
    void loadUserByUsername_semUsuarioCorrespondente_lancaUsernameNotFoundException() {
        when(usuarioRepository.findByEmail("naoexiste@mhr.com.br")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("naoexiste@mhr.com.br"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
