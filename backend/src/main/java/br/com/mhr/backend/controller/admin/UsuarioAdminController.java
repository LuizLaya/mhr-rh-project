package br.com.mhr.backend.controller.admin;

import java.util.List;
import java.util.UUID;

import br.com.mhr.backend.dto.ConvidarUsuarioResponseDto;
import br.com.mhr.backend.dto.UsuarioDto;
import br.com.mhr.backend.dto.request.AtualizarAcessoUsuarioRequest;
import br.com.mhr.backend.dto.request.ConvidarUsuarioRequest;
import br.com.mhr.backend.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Gestão de usuários do painel admin: listar, convidar e ativar/desativar acesso.
 *
 * <p>Restrito a ADMIN ({@code @PreAuthorize}) — diferente dos outros controllers
 * {@code /api/admin/**}, que qualquer usuário autenticado (ADMIN ou EDITOR) acessa.
 * Um EDITOR poder convidar/desativar outros usuários seria uma falha de autorização.
 */
@RestController
@RequestMapping("/api/admin/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioAdminController {

    private final UsuarioService service;

    @GetMapping
    public List<UsuarioDto> listar() {
        return service.listar();
    }

    @PostMapping("/convidar")
    @ResponseStatus(HttpStatus.CREATED)
    public ConvidarUsuarioResponseDto convidar(@Valid @RequestBody ConvidarUsuarioRequest request, Authentication authentication) {
        return service.convidar(request.nome(), request.email(), request.papel(), authentication.getName());
    }

    @PatchMapping("/{id}/acesso")
    public UsuarioDto atualizarAcesso(
            @PathVariable UUID id,
            @Valid @RequestBody AtualizarAcessoUsuarioRequest request,
            Authentication authentication) {
        return service.atualizarAcesso(id, request.ativo(), authentication.getName());
    }
}
