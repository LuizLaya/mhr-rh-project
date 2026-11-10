package br.com.mhr.backend.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

/**
 * Guarda apenas o hash (SHA-256) do token de "esqueci minha senha" enviado por e-mail,
 * nunca o valor em si. Mapeia a tabela {@code tokens_redefinicao_senha}.
 */
@Entity
@Table(name = "tokens_redefinicao_senha")
@Getter
@Setter
@NoArgsConstructor
public class TokenRedefinicaoSenha {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    private String tokenHash;

    @Column(name = "expira_em", nullable = false)
    private Instant expiraEm;

    @Column(nullable = false)
    private boolean usado = false;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private Instant criadoEm;
}
