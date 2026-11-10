-- Move a lista de palavras-chave do PNCP de application.yml pro banco, pra o admin
-- poder ajustar pela tela de Licitacoes sem precisar de mim/redeploy.
alter table configuracao_licitacoes add column palavras_chave text;

update configuracao_licitacoes
set palavras_chave = 'recursos humanos,recrutamento,seleção de pessoal,consultoria em rh,treinamento,terceirização de mão de obra,gestão de pessoas,recrutamento e seleção,avaliação de desempenho,desenvolvimento organizacional'
where id = '00000000-0000-0000-0000-000000000001';

alter table configuracao_licitacoes alter column palavras_chave set not null;
