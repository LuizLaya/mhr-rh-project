create table equipe (
    id uuid primary key default gen_random_uuid(),
    nome varchar(150) not null,
    cargo varchar(150),
    foto_url varchar(500),
    bio text,
    ordem_exibicao int not null default 0,
    publicado boolean not null default true,
    criado_em timestamptz not null default now(),
    atualizado_em timestamptz not null default now()
);
