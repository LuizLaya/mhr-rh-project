package br.com.mhr.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Corpo de {@code POST /api/auth/login}. */
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String senha
) {
}
