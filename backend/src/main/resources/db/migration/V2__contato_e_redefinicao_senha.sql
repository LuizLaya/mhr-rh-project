-- Contato: telefone passa a existir e mensagem vira opcional (ajuste pedido apos
-- uso real do formulario); concluido_em registra quando o lead foi marcado como
-- CONCLUIDO, para acompanhamento no admin.
alter table leads_contato
    add column telefone varchar(30),
    alter column mensagem drop not null,
    add column concluido_em timestamptz;

-- Suporte ao fluxo "esqueci minha senha" do painel admin: guarda só o hash do token
-- (nunca o token em si), com expiracao curta e uso unico.
create table tokens_redefinicao_senha (
    id uuid primary key default gen_random_uuid(),
    usuario_id uuid not null references usuarios(id) on delete cascade,
    token_hash varchar(255) not null unique,
    expira_em timestamptz not null,
    usado boolean not null default false,
    criado_em timestamptz not null default now()
);

create index idx_tokens_redefinicao_usuario on tokens_redefinicao_senha(usuario_id);
