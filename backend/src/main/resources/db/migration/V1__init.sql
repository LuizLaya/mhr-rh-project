-- Schema inicial do MHR: usuarios do painel, conteudo institucional (servicos,
-- depoimentos, posts de blog), vagas e seus respectivos leads (candidaturas e
-- mensagens de contato). Nomes de tabelas/colunas em portugues, por padrao do projeto.

create extension if not exists pgcrypto;

create table usuarios (
    id uuid primary key default gen_random_uuid(),
    nome varchar(150) not null,
    email varchar(180) not null unique,
    senha_hash varchar(255) not null,
    papel varchar(20) not null check (papel in ('ADMIN', 'EDITOR')),
    criado_em timestamptz not null default now()
);

create table servicos (
    id uuid primary key default gen_random_uuid(),
    titulo varchar(150) not null,
    slug varchar(170) not null unique,
    resumo varchar(300) not null,
    conteudo text not null,
    icone varchar(60),
    ordem_exibicao int not null default 0,
    publicado boolean not null default true,
    criado_em timestamptz not null default now(),
    atualizado_em timestamptz not null default now()
);

create table vagas (
    id uuid primary key default gen_random_uuid(),
    titulo varchar(150) not null,
    slug varchar(170) not null unique,
    descricao text not null,
    cidade varchar(100) not null,
    modelo_trabalho varchar(20) not null check (modelo_trabalho in ('PRESENCIAL', 'HIBRIDO', 'REMOTO')),
    status varchar(20) not null default 'ABERTA' check (status in ('ABERTA', 'ENCERRADA')),
    publicado_em timestamptz,
    criado_em timestamptz not null default now(),
    atualizado_em timestamptz not null default now()
);

create table candidaturas (
    id uuid primary key default gen_random_uuid(),
    vaga_id uuid not null references vagas(id) on delete cascade,
    nome_candidato varchar(150) not null,
    email_candidato varchar(180) not null,
    telefone_candidato varchar(30),
    url_curriculo varchar(500) not null,
    consentimento_lgpd boolean not null default false,
    criado_em timestamptz not null default now()
);

create index idx_candidaturas_vaga on candidaturas(vaga_id);

create table posts_blog (
    id uuid primary key default gen_random_uuid(),
    titulo varchar(200) not null,
    slug varchar(220) not null unique,
    resumo varchar(400) not null,
    conteudo text not null,
    url_imagem_capa varchar(500),
    categoria varchar(80),
    publicado boolean not null default false,
    publicado_em timestamptz,
    criado_em timestamptz not null default now(),
    atualizado_em timestamptz not null default now()
);

create table depoimentos (
    id uuid primary key default gen_random_uuid(),
    nome_autor varchar(150) not null,
    cargo_autor varchar(150),
    nome_empresa varchar(150),
    conteudo text not null,
    url_foto varchar(500),
    ordem_exibicao int not null default 0,
    publicado boolean not null default true,
    criado_em timestamptz not null default now()
);

create table leads_contato (
    id uuid primary key default gen_random_uuid(),
    nome varchar(150) not null,
    email varchar(180) not null,
    nome_empresa varchar(150),
    mensagem text not null,
    status varchar(20) not null default 'NOVO' check (status in ('NOVO', 'EM_ANDAMENTO', 'CONCLUIDO')),
    criado_em timestamptz not null default now()
);
