-- Acompanhamento de licitacoes publicas (pregoes, editais) que a MHR pode
-- participar. Licitacoes podem ser importadas automaticamente do PNCP
-- (Portal Nacional de Contratacoes Publicas) ou cadastradas manualmente.
create table licitacoes (
    id uuid primary key default gen_random_uuid(),
    numero_controle_pncp varchar(60) unique,
    origem varchar(20) not null check (origem in ('PNCP_AUTOMATICO', 'MANUAL')),
    orgao varchar(255) not null,
    objeto text not null,
    modalidade varchar(100),
    numero_processo varchar(100),
    uf varchar(2),
    valor_estimado numeric(14,2),
    data_abertura_proposta timestamptz,
    data_encerramento_proposta timestamptz,
    link_edital varchar(500),
    responsavel_id uuid references usuarios(id) on delete set null,
    status varchar(20) not null default 'IDENTIFICADA' check (status in
        ('IDENTIFICADA', 'EM_ANALISE', 'DOCUMENTACAO', 'PROPOSTA_ENVIADA', 'GANHOU', 'PERDEU', 'DESCARTADA')),
    observacoes text,
    criado_em timestamptz not null default now(),
    atualizado_em timestamptz not null default now()
);

create index idx_licitacoes_status on licitacoes(status);

-- Checklist de documentos exigidos pelo edital (certidoes, atestados...).
create table documentos_licitacao (
    id uuid primary key default gen_random_uuid(),
    licitacao_id uuid not null references licitacoes(id) on delete cascade,
    nome varchar(255) not null,
    concluido boolean not null default false,
    criado_em timestamptz not null default now()
);

create index idx_documentos_licitacao_licitacao on documentos_licitacao(licitacao_id);

-- Linha unica (id fixo) com a configuracao do sync automatico com o PNCP.
-- Fica no banco (nao em application.yml) porque o admin liga/desliga pelo
-- proprio painel, sem precisar de redeploy.
create table configuracao_licitacoes (
    id uuid primary key,
    busca_pncp_ativa boolean not null default false,
    atualizado_em timestamptz not null default now()
);

insert into configuracao_licitacoes (id, busca_pncp_ativa)
values ('00000000-0000-0000-0000-000000000001', false);
