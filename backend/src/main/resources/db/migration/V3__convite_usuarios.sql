-- Suporte a convite de novos usuarios do painel admin: um usuario convidado e criado
-- sem senha utilizavel ate aceitar o convite por e-mail (senha_definida = false).
-- "ativo" permite revogar o acesso sem apagar o historico (quem criou o que).
alter table usuarios
    add column ativo boolean not null default true,
    add column senha_definida boolean not null default true,
    add column convidado_por uuid references usuarios(id) on delete set null;
