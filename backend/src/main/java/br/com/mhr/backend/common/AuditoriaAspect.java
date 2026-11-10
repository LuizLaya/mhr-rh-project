package br.com.mhr.backend.common;

import br.com.mhr.backend.service.LogSistemaService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Registra em {@code log_sistema} (categoria {@code AUDITORIA}) toda chamada que
 * muda dado num controller admin (POST/PUT/PATCH/DELETE) - quem fez e o que foi
 * chamado. Só dispara em sucesso ({@code @AfterReturning}); uma tentativa que falhou
 * já fica registrada pelos outros caminhos (ApiExceptionHandler).
 *
 * <p>É deliberadamente um aspecto (em vez de uma chamada manual em cada service):
 * cobre todo controller admin, presente e futuro, sem precisar lembrar de adicionar
 * o log em cada método novo - só não é uma trilha de auditoria completa (não grava
 * o payload antes/depois, só "quem mudou o quê"), mesma limitação já documentada do
 * log_sistema como um todo.</p>
 */
@Aspect
@Component
@RequiredArgsConstructor
public class AuditoriaAspect {

    private final LogSistemaService logSistemaService;

    @AfterReturning("execution(* br.com.mhr.backend.controller.admin..*.*(..)) && "
            + "(@annotation(org.springframework.web.bind.annotation.PostMapping) "
            + "|| @annotation(org.springframework.web.bind.annotation.PutMapping) "
            + "|| @annotation(org.springframework.web.bind.annotation.PatchMapping) "
            + "|| @annotation(org.springframework.web.bind.annotation.DeleteMapping))")
    public void registrarAlteracao(JoinPoint joinPoint) {
        String origem = joinPoint.getSignature().getDeclaringType().getSimpleName() + "." + joinPoint.getSignature().getName();
        logSistemaService.info("AUDITORIA", origem + " executado com sucesso", obterEmailAutenticado());
    }

    private String obterEmailAutenticado() {
        var autenticacao = SecurityContextHolder.getContext().getAuthentication();
        return autenticacao != null ? autenticacao.getName() : null;
    }
}
