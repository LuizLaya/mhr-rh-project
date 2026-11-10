package br.com.mhr.backend.dto;

import br.com.mhr.backend.model.Usuario;

/** Resposta de um login bem-sucedido: o JWT e os dados exibidos na UI do painel. */
public record LoginResponse(String token, long expiraEmMinutos, String nome, Usuario.Papel papel) {
}
