package br.com.mhr.backend.controller;

import br.com.mhr.backend.dto.LoginResponse;
import br.com.mhr.backend.dto.request.EsqueciSenhaRequest;
import br.com.mhr.backend.dto.request.LoginRequest;
import br.com.mhr.backend.dto.request.RedefinirSenhaRequest;
import br.com.mhr.backend.model.Usuario;
import br.com.mhr.backend.repository.UsuarioRepository;
import br.com.mhr.backend.security.JwtService;
import br.com.mhr.backend.service.LogSistemaService;
import br.com.mhr.backend.service.RedefinicaoSenhaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Login do painel admin e fluxo de "esqueci minha senha". */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final RedefinicaoSenhaService redefinicaoSenhaService;
    private final LogSistemaService logSistemaService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.senha()));
        } catch (AuthenticationException e) {
            logSistemaService.aviso("LOGIN", "Tentativa de login invalida", request.email());
            throw e;
        }

        // Se a linha acima nao lancou AuthenticationException, o usuario existe e a senha confere.
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Credenciais invalidas"));

        String token = jwtService.gerarToken(usuario);
        logSistemaService.info("LOGIN", "Login bem-sucedido", usuario.getEmail());
        return new LoginResponse(token, jwtService.expiracaoMinutos(), usuario.getNome(), usuario.getPapel());
    }

    // Sempre retorna 202, exista ou nao o e-mail: evita que alguem descubra quais
    // e-mails tem cadastro no painel so testando esse endpoint.
    @PostMapping("/esqueci-senha")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void esqueciSenha(@Valid @RequestBody EsqueciSenhaRequest request) {
        redefinicaoSenhaService.solicitar(request.email());
    }

    @PostMapping("/redefinir-senha")
    public void redefinirSenha(@Valid @RequestBody RedefinirSenhaRequest request) {
        redefinicaoSenhaService.redefinir(request.token(), request.novaSenha());
    }
}
