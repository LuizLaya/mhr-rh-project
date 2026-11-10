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
 * Candidatura de alguém a uma {@link Vaga}. Mapeia a tabela
 * {@code candidaturas}; referencia a vaga só pelo id (sem @ManyToOne) para manter os
 * módulos desacoplados — a validação de que a vaga existe fica no controller.
 */
@Entity
@Table(name = "candidaturas")
@Getter
@Setter
@NoArgsConstructor
public class Candidatura {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "vaga_id", nullable = false)
    private UUID vagaId;

    @Column(name = "nome_candidato", nullable = false, length = 150)
    private String nomeCandidato;

    @Column(name = "email_candidato", nullable = false, length = 180)
    private String emailCandidato;

    @Column(name = "telefone_candidato", length = 30)
    private String telefoneCandidato;

    @Column(name = "url_curriculo", nullable = false, length = 500)
    private String urlCurriculo;

    @Column(name = "consentimento_lgpd", nullable = false)
    private boolean consentimentoLgpd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusCandidatura status = StatusCandidatura.NOVA;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private Instant criadoEm;

    public enum StatusCandidatura {
        NOVA, EM_ANALISE, APROVADO, REJEITADO
    }
}
