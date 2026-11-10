-- Clientes: empresas com contrato fechado com a MHR. Depoimentos podem estar
-- vinculados a um cliente (quem deu o depoimento), e uma mensagem de contato pode
-- ser marcada como "virou contrato" e apontar para o cliente criado a partir dela.
create table clientes (
    id uuid primary key default gen_random_uuid(),
    nome_empresa varchar(150) not null,
    cnpj varchar(20),
    contato_nome varchar(150),
    contato_email varchar(180),
    contato_telefone varchar(30),
    observacoes text,
    criado_em timestamptz not null default now()
);

alter table depoimentos
    add column cliente_id uuid references clientes(id) on delete set null;

alter table leads_contato
    add column virou_contrato boolean not null default false,
    add column cliente_id uuid references clientes(id) on delete set null;
