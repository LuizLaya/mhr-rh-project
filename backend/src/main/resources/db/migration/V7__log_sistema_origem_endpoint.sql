-- Enriquece log_sistema com de onde o evento veio, para a tela de Logs do admin
-- mostrar isso em vez de só a mensagem (ver docs/ANALISE_TECNICA.md).
alter table log_sistema add column origem varchar(255);
alter table log_sistema add column endpoint varchar(255);
