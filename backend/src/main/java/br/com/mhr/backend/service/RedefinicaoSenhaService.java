package br.com.mhr.backend.service;

import java.util.UUID;

import br.com.mhr.backend.model.Usuario;
import br.com.mhr.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Fluxo de "esqueci minha senha" do painel admin: solicita e, depois, efetiva a troca. */
@Service
@RequiredArgsConstructor
public class RedefinicaoSenhaService {

    private final UsuarioRepository usuarioRepository;
    private final TokenAcessoService tokenAcessoService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.frontend-admin-url}")
    private String frontendAdminUrl;

    /**
     * Nunca revela se o e-mail existe ou está ativo: do ponto de vista de quem chama,
     * esta operação sempre "funciona" (evita enumeração de usuários cadastrados).
     */
    public void solicitar(String email) {
        usuarioRepository.findByEmail(email)
                .filter(Usuario::isAtivo)
                .ifPresent(usuario -> {
                    String tokenBruto = tokenAcessoService.gerarToken(usuario.getId());
                    String link = frontendAdminUrl + "/redefinir-senha?token=" + tokenBruto;
                    emailService.enviarRedefinicaoSenha(usuario.getEmail(), link);
                });
    }

    /** Usado tanto por "esqueci minha senha" quanto por "aceitar convite" (ver {@link Usuario}). */
    @Transactional
    public void redefinir(String tokenBruto, String novaSenha) {
        UUID usuarioId = tokenAcessoService.consumir(tokenBruto);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .filter(Usuario::isAtivo)
                .orElseThrow(() -> new IllegalArgumentException("Token invalido ou expirado"));

        usuario.setSenhaHash(passwordEncoder.encode(novaSenha));
        usuario.setSenhaDefinida(true);
        usuarioRepository.save(usuario);
    }
}
