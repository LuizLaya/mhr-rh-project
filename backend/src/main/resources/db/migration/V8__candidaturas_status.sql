-- Permite o admin acompanhar cada candidatura (ver docs/ANALISE_PRODUTO_ROADMAP.md).
alter table candidaturas add column status varchar(20) not null default 'NOVA';
