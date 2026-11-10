create table faq (
    id uuid primary key default gen_random_uuid(),
    pergunta varchar(300) not null,
    resposta text not null,
    ordem_exibicao int not null default 0,
    publicado boolean not null default true,
    criado_em timestamptz not null default now(),
    atualizado_em timestamptz not null default now()
);
