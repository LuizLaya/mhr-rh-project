package br.com.mhr.backend.dto;

import java.time.Instant;
import java.util.UUID;

import br.com.mhr.backend.model.Usuario;

/** Representação de um usuário do painel admin, para a tela de gestão de usuários. */
public record UsuarioDto(
        UUID id,
        String nome,
        String email,
        Usuario.Papel papel,
        boolean ativo,
        boolean senhaDefinida,
        Instant criadoEm
) {
}
