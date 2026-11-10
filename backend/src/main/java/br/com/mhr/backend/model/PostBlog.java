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

/** Artigo do blog institucional. Mapeia a tabela {@code posts_blog}. */
@Entity
@Table(name = "posts_blog")
@Getter
@Setter
@NoArgsConstructor
public class PostBlog {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, unique = true, length = 220)
    private String slug;

    @Column(nullable = false, length = 400)
    private String resumo;

    @Column(nullable = false, columnDefinition = "text")
    private String conteudo;

    @Column(name = "url_imagem_capa", length = 500)
    private String urlImagemCapa;

    @Column(length = 80)
    private String categoria;

    @Column(nullable = false)
    private boolean publicado = false;

    @Column(name = "publicado_em")
    private Instant publicadoEm;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private Instant criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private Instant atualizadoEm;
}
