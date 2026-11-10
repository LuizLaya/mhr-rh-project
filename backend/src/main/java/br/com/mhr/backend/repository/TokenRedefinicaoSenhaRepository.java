package br.com.mhr.backend.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import br.com.mhr.backend.model.TokenRedefinicaoSenha;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso a dados de {@link TokenRedefinicaoSenha}. */
public interface TokenRedefinicaoSenhaRepository extends JpaRepository<TokenRedefinicaoSenha, UUID> {

    Optional<TokenRedefinicaoSenha> findByTokenHashAndUsadoFalseAndExpiraEmAfter(String tokenHash, Instant agora);
}
