package br.com.mhr.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Corpo de {@code POST /api/auth/redefinir-senha} — mesmo endpoint para "esqueci senha" e "aceitar convite". */
public record RedefinirSenhaRequest(
        @NotBlank String token,
        @NotBlank @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres") String novaSenha
) {
}
