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

/** Mensagem enviada pelo formulário de contato do site. Mapeia a tabela {@code leads_contato}. */
@Entity
@Table(name = "leads_contato")
@Getter
@Setter
@NoArgsConstructor
public class LeadContato {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 180)
    private String email;

    @Column(length = 30)
    private String telefone;

    @Column(name = "nome_empresa", length = 150)
    private String nomeEmpresa;

    @Column(columnDefinition = "text")
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusLead status = StatusLead.NOVO;

    @Column(name = "concluido_em")
    private Instant concluidoEm;

    @Column(name = "virou_contrato", nullable = false)
    private boolean virouContrato = false;

    // Cliente vinculado quando o lead vira contrato. Sem relacionamento JPA, mesmo
    // criterio do Depoimento: so o id, o front-end resolve o nome cruzando com a
    // lista de clientes ja carregada.
    @Column(name = "cliente_id")
    private UUID clienteId;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private Instant criadoEm;

    public enum StatusLead {
        NOVO, EM_ANDAMENTO, CONCLUIDO
    }
}
