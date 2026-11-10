package br.com.mhr.backend.security;

import java.util.List;

import br.com.mhr.backend.model.Usuario;
import br.com.mhr.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Carrega o usuário (pelo e-mail) para o Spring Security autenticar o login.
 *
 * <p>{@code disabled(!usuario.isAtivo())} faz o Spring Security barrar sozinho o login
 * de usuários desativados (lança {@code DisabledException}, que vira 401 genérico em
 * {@link br.com.mhr.backend.common.ApiExceptionHandler} — não entrega se a conta existe).
 */
@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado: " + email));

        return User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash())
                .disabled(!usuario.isAtivo())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getPapel().name())))
                .build();
    }
}
