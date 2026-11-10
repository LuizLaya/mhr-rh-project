package br.com.mhr.backend.dto.request;

import br.com.mhr.backend.controller.admin.UsuarioAdminController;

/** Ativa/desativa o acesso de um usuário ao painel admin (ver {@link UsuarioAdminController}). */
public record AtualizarAcessoUsuarioRequest(boolean ativo) {
}
