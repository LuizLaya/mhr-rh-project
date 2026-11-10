package br.com.mhr.backend.common;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

import br.com.mhr.backend.service.LogSistemaService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Converte exceções de domínio em respostas JSON padronizadas, em vez do stacktrace padrão do Spring. */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ApiExceptionHandler {

    private final LogSistemaService logSistemaService;

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleNaoEncontrado(RecursoNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(corpo(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleRequisicaoInvalida(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpo(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    // Sem isso, uma falha de @Valid (ex.: @NotBlank, @Email) caia no catch-all de baixo
    // e virava 500 "erro interno do servidor" em vez de um 400 com a mensagem de campo
    // certa - bug real, encontrado testando o formulario publico de contato ao vivo.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidacao(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpo(HttpStatus.BAD_REQUEST, mensagem));
    }

    // 401/403 valem um AVISO (nao um ERRO): sao relevantes pra observabilidade de
    // seguranca sem exigir investigacao imediata como um 500. Os demais 4xx (404/400)
    // ficam de fora de proposito - sao, na maioria das vezes, so o cliente pedindo algo
    // que nao existe ou mandando um dado invalido, nao um sinal de problema no sistema,
    // e logar todos eles encheria a tela de Logs de ruido.
    //
    // O caso mais comum de 401 (acessar uma rota protegida sem nenhum JWT valido) nunca
    // chega aqui - e barrado antes, na cadeia de filtros do Spring Security, e logado
    // por AutenticacaoNecessariaEntryPoint. Os handlers abaixo cobrem os outros dois
    // casos, que sim passam pelo Spring MVC: credenciais erradas no login
    // (AuthenticationException lancada direto por AuthController) e um usuario
    // autenticado mas sem o papel exigido por um @PreAuthorize (AccessDeniedException).
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAutenticacao(AuthenticationException ex, HttpServletRequest request) {
        logSistemaService.aviso("AUTENTICACAO", "Falha de autenticacao: " + ex.getMessage(), null,
                request.getMethod() + " " + request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(corpo(HttpStatus.UNAUTHORIZED, "Credenciais invalidas"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAcessoNegado(AccessDeniedException ex, HttpServletRequest request) {
        logSistemaService.aviso("ACESSO", "Acesso negado", null, request.getMethod() + " " + request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(corpo(HttpStatus.FORBIDDEN, "Acesso negado"));
    }

    // Ultima linha de defesa contra condicoes de corrida em cima de constraints unicas
    // do banco (ex.: dois convites simultaneos para o mesmo e-mail) — sem isso, vira
    // 500 com stacktrace em vez de uma resposta de erro decente.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleIntegridade(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(corpo(HttpStatus.CONFLICT, "Conflito de dados: registro ja existe ou esta em uso"));
    }

    // Ultima linha de defesa contra qualquer excecao nao mapeada acima - sem isso, o
    // Spring devolve o whitelabel error padrao (em ingles, com stacktrace), o que viola
    // a regra do projeto de toda mensagem de erro aparecer em portugues no front-end.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleErroInesperado(Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado nao tratado", ex);
        String endpoint = request.getMethod() + " " + request.getRequestURI();
        logSistemaService.erro("SISTEMA", ex.getClass().getSimpleName() + ": " + ex.getMessage(), null, endpoint);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(corpo(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor. Tente novamente mais tarde."));
    }

    private Map<String, Object> corpo(HttpStatus status, String mensagem) {
        return Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", mensagem
        );
    }
}
