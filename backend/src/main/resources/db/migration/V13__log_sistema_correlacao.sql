-- Agrupa as varias linhas de log de uma mesma execucao de rotina agendada
-- (ex.: sincronizacao do PNCP, alerta diario de leads/licitacoes).
alter table log_sistema add column correlacao_id varchar(36);
