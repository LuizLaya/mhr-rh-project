package br.com.mhr.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Corpo de {@code POST /api/auth/esqueci-senha}. */
public record EsqueciSenhaRequest(@NotBlank @Email String email) {
}
