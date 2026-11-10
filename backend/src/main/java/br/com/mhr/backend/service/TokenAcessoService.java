package br.com.mhr.backend.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

import br.com.mhr.backend.model.TokenRedefinicaoSenha;
import br.com.mhr.backend.repository.TokenRedefinicaoSenhaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gera e consome os tokens de uso único usados em "esqueci minha senha" e em
 * "aceitar convite do painel" — as duas operações são, na prática, a mesma coisa:
 * "usando este token, defina uma senha para este usuário". Guarda só o hash
 * (SHA-256) do token, nunca o valor em si.
 */
@Service
@RequiredArgsConstructor
public class TokenAcessoService {

    private static final long VALIDADE_MINUTOS = 30;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final TokenRedefinicaoSenhaRepository tokenRepository;

    /** Cria um token para o usuário e devolve o valor bruto (vai no link do e-mail). */
    @Transactional
    public String gerarToken(UUID usuarioId) {
        String tokenBruto = tokenAleatorio();

        TokenRedefinicaoSenha token = new TokenRedefinicaoSenha();
        token.setUsuarioId(usuarioId);
        token.setTokenHash(hash(tokenBruto));
        token.setExpiraEm(Instant.now().plus(VALIDADE_MINUTOS, ChronoUnit.MINUTES));
        tokenRepository.save(token);

        return tokenBruto;
    }

    /**
     * Valida o token, marca como usado (uso único) e devolve o id do usuário associado.
     *
     * @throws IllegalArgumentException se o token for inválido, expirado ou já usado
     */
    @Transactional
    public UUID consumir(String tokenBruto) {
        TokenRedefinicaoSenha token = tokenRepository
                .findByTokenHashAndUsadoFalseAndExpiraEmAfter(hash(tokenBruto), Instant.now())
                .orElseThrow(() -> new IllegalArgumentException("Token invalido ou expirado"));

        token.setUsado(true);
        tokenRepository.save(token);
        return token.getUsuarioId();
    }

    private String tokenAleatorio() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String valor) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(valor.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponivel na JVM", e);
        }
    }
}
