package br.com.mhr.backend.service.turnstile;

/** Resposta de {@code POST https://challenges.cloudflare.com/turnstile/v0/siteverify} (só o campo usado aqui). */
public record TurnstileRespostaDto(boolean success) {
}
