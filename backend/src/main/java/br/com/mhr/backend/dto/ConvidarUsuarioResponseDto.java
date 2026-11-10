package br.com.mhr.backend.dto;

import br.com.mhr.backend.service.ConviteUsuarioService;

/**
 * Resposta de {@code POST /api/admin/usuarios/convidar}: o usuário criado e se o
 * e-mail de convite realmente saiu (ver {@link ConviteUsuarioService.ResultadoConvite}).
 * Quando {@code emailEnviado} é {@code false}, o admin precisa repassar o link
 * manualmente (ele fica registrado em "Logs", categoria CONVITE).
 */
public record ConvidarUsuarioResponseDto(UsuarioDto usuario, boolean emailEnviado) {
}
