package br.com.mhr.backend.config;

import br.com.mhr.backend.security.AutenticacaoNecessariaEntryPoint;
import br.com.mhr.backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Regras de autorização da API.
 *
 * <p>Tudo que NÃO está explicitamente listado como público exige um JWT válido
 * (fail-closed: {@code anyRequest().authenticated()}). Os endpoints de escrita/gestão
 * de cada módulo vivem sob {@code /api/admin/**}, cobertos por essa regra padrão —
 * isso basta para "precisa estar logado", mas não distingue ADMIN de EDITOR.
 *
 * <p>{@code @EnableMethodSecurity} liga o {@code @PreAuthorize} usado em controllers
 * como {@link br.com.mhr.backend.controller.admin.UsuarioAdminController}, para restringir
 * endpoints específicos a um papel (ex.: só ADMIN gerencia outros usuários).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AutenticacaoNecessariaEntryPoint autenticacaoNecessariaEntryPoint;

    // Leitura publica do site institucional (vagas, blog, depoimentos, servicos) e os
    // dois formularios publicos (candidatura e contato). Login tambem e publico: e ele
    // quem entrega o token usado em tudo o que exige autenticacao.
    private static final String[] ENDPOINTS_PUBLICOS_GET = {
            "/api/vagas/**",
            "/api/posts-blog/**",
            "/api/depoimentos/**",
            "/api/servicos/**",
            "/api/clientes-destaque",
            "/api/equipe",
            "/api/faq",
            "/uploads/**",
            "/docs/**",
            "/v3/api-docs/**",
            "/actuator/health"
    };

    private static final String[] ENDPOINTS_PUBLICOS_POST = {
            "/api/auth/login",
            "/api/auth/esqueci-senha",
            "/api/auth/redefinir-senha",
            "/api/vagas/*/candidaturas",
            "/api/leads-contato"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(autenticacaoNecessariaEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, ENDPOINTS_PUBLICOS_GET).permitAll()
                        .requestMatchers(HttpMethod.POST, ENDPOINTS_PUBLICOS_POST).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            org.springframework.security.core.userdetails.UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
