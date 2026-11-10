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
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

/**
 * Vaga de emprego divulgada no site da MHR e trabalhada pela consultoria.
 * Mapeia a tabela {@code vagas} (ver V1__init.sql).
 */
@Entity
@Table(name = "vagas")
@Getter
@Setter
@NoArgsConstructor
public class Vaga {

    // Hibernate mapeia java.util.UUID para a coluna nativa "uuid" do Postgres sem
    // conversao extra. @UuidGenerator gera o valor em memoria no insert (nao depende
    // do default gen_random_uuid() do banco, que so cobre inserts feitos fora do JPA).
    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, unique = true, length = 170)
    private String slug;

    @Column(nullable = false, columnDefinition = "text")
    private String descricao;

    @Column(nullable = false, length = 100)
    private String cidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "modelo_trabalho", nullable = false, length = 20)
    private ModeloTrabalho modeloTrabalho;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusVaga status = StatusVaga.ABERTA;

    @Column(name = "publicado_em")
    private Instant publicadoEm;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private Instant criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private Instant atualizadoEm;

    public enum ModeloTrabalho {
        PRESENCIAL, HIBRIDO, REMOTO
    }

    public enum StatusVaga {
        ABERTA, ENCERRADA
    }
}
