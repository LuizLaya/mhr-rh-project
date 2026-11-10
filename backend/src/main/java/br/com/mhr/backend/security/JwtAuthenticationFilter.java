package br.com.mhr.backend.security;

import java.io.IOException;
import java.util.List;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Lê o header {@code Authorization: Bearer <token>}, valida o JWT e autentica a requisição. */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIXO_BEARER = "Bearer ";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(PREFIXO_BEARER) && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Claims claims = jwtService.validar(header.substring(PREFIXO_BEARER.length()));
                String email = claims.getSubject();
                String papel = claims.get("papel", String.class);

                var authentication = new UsernamePasswordAuthenticationToken(
                        email, null, List.of(new SimpleGrantedAuthority("ROLE_" + papel)));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException ignored) {
                // Token ausente/invalido/expirado: segue sem autenticar: SecurityConfig
                // devolve 401/403 para rotas protegidas quando necessario.
            }
        }

        filterChain.doFilter(request, response);
    }
}
