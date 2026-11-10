package br.com.mhr.backend.dto.request;

import br.com.mhr.backend.model.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Corpo de {@code POST /api/admin/usuarios/convidar}. */
public record ConvidarUsuarioRequest(
        @NotBlank String nome,
        @NotBlank @Email String email,
        @NotNull Usuario.Papel papel
) {
}
