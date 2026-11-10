package br.com.mhr.backend.dto;

import java.util.UUID;

/**
 * Nome e id de um usuário ativo, para seletores como "responsável" em
 * {@code Licitacao} — não expõe e-mail/papel/status, que são dados restritos a
 * ADMIN em {@code UsuarioAdminController}.
 */
public record UsuarioResumoDto(UUID id, String nome) {
}
