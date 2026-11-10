-- Log de eventos do sistema (login, envio de e-mail, erros inesperados), usado pela
-- tela "Logs" do painel admin. Fica com o registro bem enxuto de proposito: e para
-- diagnostico rapido, nao para auditoria completa (ver LogSistemaService para a
-- limpeza automatica semanal, que mantem essa tabela pequena).
create table log_sistema (
    id uuid primary key default gen_random_uuid(),
    nivel varchar(10) not null check (nivel in ('INFO', 'AVISO', 'ERRO')),
    categoria varchar(50) not null,
    mensagem text not null,
    usuario_email varchar(180),
    criado_em timestamptz not null default now()
);

create index idx_log_sistema_criado_em on log_sistema(criado_em desc);
