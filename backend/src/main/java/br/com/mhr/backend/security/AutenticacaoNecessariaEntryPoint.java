package br.com.mhr.backend.security;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

import br.com.mhr.backend.service.LogSistemaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Devolve 401 em JSON (em vez do {@link org.springframework.security.web.access.Http403ForbiddenEntryPoint}
 * default) quando uma rota protegida é acessada sem um JWT válido, e loga o evento.
 *
 * <p>Esse caso (sem token/token invalido/expirado) é tratado aqui, na cadeia de
 * filtros do Spring Security, porque nunca chega a entrar no Spring MVC — logo não
 * passa pelo {@link br.com.mhr.backend.common.ApiExceptionHandler}, que só cobre o
 * outro caso de acesso negado (usuário autenticado mas sem o papel exigido por um
 * {@code @PreAuthorize}, ex. EDITOR tentando acessar algo só de ADMIN).</p>
 */
// ObjectMapper e' instanciado direto (em vez de @Autowired) porque o conjunto de
// dependencias enxuto deste projeto nao expoe um bean ObjectMapper autowireable em
// todos os contextos de teste (mesmo caso ja visto com RestClient.Builder).
@Component
@RequiredArgsConstructor
public class AutenticacaoNecessariaEntryPoint implements AuthenticationEntryPoint {

    private final LogSistemaService logSistemaService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        logSistemaService.aviso("AUTENTICACAO", "Acesso sem token valido", null, request.getMethod() + " " + request.getRequestURI());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(Map.of(
                "timestamp", Instant.now().toString(),
                "status", HttpStatus.UNAUTHORIZED.value(),
                "error", HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "message", "Autenticacao necessaria"
        )));
    }
}
