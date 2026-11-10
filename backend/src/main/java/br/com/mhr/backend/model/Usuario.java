package br.com.mhr.backend.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

/**
 * Usuário do painel administrativo (login via JWT). Mapeia a tabela {@code usuarios}.
 *
 * <p>Um usuário convidado (ver {@code br.com.mhr.backend.service.ConviteUsuarioService})
 * nasce com {@code senhaDefinida = false} e uma senha aleatória inutilizável: ele só
 * consegue logar depois de abrir o link do convite e escolher a própria senha, via
 * {@code POST /api/auth/redefinir-senha} (o mesmo endpoint usado por "esqueci minha
 * senha" — tecnicamente são a mesma operação: "defina uma senha usando este token").
 * {@code ativo = false} revoga o acesso sem apagar o histórico de quem criou o quê.
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 180)
    private String email;

    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Papel papel;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "senha_definida", nullable = false)
    private boolean senhaDefinida = true;

    @Column(name = "convidado_por")
    private UUID convidadoPor;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private Instant criadoEm;

    public enum Papel {
        ADMIN, EDITOR
    }
}
