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

/** Depoimento de cliente exibido no site. Mapeia a tabela {@code depoimentos}. */
@Entity
@Table(name = "depoimentos")
@Getter
@Setter
@NoArgsConstructor
public class Depoimento {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "nome_autor", nullable = false, length = 150)
    private String nomeAutor;

    @Column(name = "cargo_autor", length = 150)
    private String cargoAutor;

    @Column(name = "nome_empresa", length = 150)
    private String nomeEmpresa;

    @Column(nullable = false, columnDefinition = "text")
    private String conteudo;

    @Column(name = "url_foto", length = 500)
    private String urlFoto;

    @Column(name = "ordem_exibicao", nullable = false)
    private int ordemExibicao;

    @Column(nullable = false)
    private boolean publicado = true;

    // Cliente que deu o depoimento, se houver um cadastrado (ver modulo cliente). So
    // o id, sem relacionamento JPA: evita acoplar o modulo de depoimentos ao de
    // clientes so para exibir nome, que o front-end resolve cruzando com a lista.
    @Column(name = "cliente_id")
    private UUID clienteId;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private Instant criadoEm;
}
