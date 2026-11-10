-- Permite escolher quais clientes aparecem na home do site (logo + nome) -
-- nunca todos automaticamente, pra nao vazar CNPJ/contato de quem nao autorizou.
alter table clientes add column logo_url varchar(500);
alter table clientes add column exibir_no_site boolean not null default false;
