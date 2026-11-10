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
 * Um item do checklist de documentos exigidos por uma {@link Licitacao} (ex.:
 * "Certidão Negativa de Débitos Federais"). Mapeia a tabela
 * {@code documentos_licitacao}.
 */
@Entity
@Table(name = "documentos_licitacao")
@Getter
@Setter
@NoArgsConstructor
public class DocumentoLicitacao {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "licitacao_id", nullable = false)
    private UUID licitacaoId;

    @Column(nullable = false, length = 255)
    private String nome;

    @Column(nullable = false)
    private boolean concluido = false;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private Instant criadoEm;
}
