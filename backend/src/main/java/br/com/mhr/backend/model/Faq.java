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
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

/** Pergunta frequente exibida no site. Mapeia a tabela {@code faq}. */
@Entity
@Table(name = "faq")
@Getter
@Setter
@NoArgsConstructor
public class Faq {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 300)
    private String pergunta;

    @Column(nullable = false, columnDefinition = "text")
    private String resposta;

    @Column(name = "ordem_exibicao", nullable = false)
    private int ordemExibicao;

    @Column(nullable = false)
    private boolean publicado = true;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private Instant criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private Instant atualizadoEm;
}
