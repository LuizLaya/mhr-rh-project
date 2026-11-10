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
 * Empresa com contrato fechado com a MHR. Pode estar vinculada a um ou mais
 * {@link Depoimento} (quem deu o depoimento) e a uma
 * {@link LeadContato} marcada como "virou contrato"
 * (de onde o cliente veio). Mapeia a tabela {@code clientes}.
 */
@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
public class Cliente {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "nome_empresa", nullable = false, length = 150)
    private String nomeEmpresa;

    @Column(length = 20)
    private String cnpj;

    @Column(name = "contato_nome", length = 150)
    private String contatoNome;

    @Column(name = "contato_email", length = 180)
    private String contatoEmail;

    @Column(name = "contato_telefone", length = 30)
    private String contatoTelefone;

    @Column(columnDefinition = "text")
    private String observacoes;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    // Clientes sao um cadastro interno (CRM) com dado sensivel (CNPJ, contato) - esse
    // flag e' o unico jeito de um cliente aparecer publicamente (home do site), e
    // comeca desligado por padrao: precisa de uma decisao explicita do admin.
    @Column(name = "exibir_no_site", nullable = false)
    private boolean exibirNoSite;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private Instant criadoEm;
}
