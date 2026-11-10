# Análise técnica — tratamento de erros, observabilidade e débito técnico

> Documento de planejamento (nenhuma mudança de código foi feita a partir
> dele ainda). Cobre o pedido específico de melhorar a tela de Logs (mostrar
> endpoint/service/função do erro) e, de forma mais ampla, outros pontos
> técnicos que vale olhar.

## 1. O pedido concreto: enriquecer `log_sistema` com origem do erro

**Hoje**: `ApiExceptionHandler.handleErroInesperado` grava em `log_sistema`
só isto:

```java
logSistemaService.erro("SISTEMA", ex.getClass().getSimpleName() + ": " + ex.getMessage(), null);
```

Ou seja, a tela de Logs mostra algo como `ERRO · SISTEMA ·
NullPointerException: null` — dá para saber QUE algo quebrou, mas não ONDE
(qual endpoint foi chamado, qual service/método estava rodando). Para
depurar, hoje eu preciso ir ao terminal do backend e procurar o stack trace
completo (que só vai pro `log.error(...)`, nunca pro banco/tela).

Outros pontos de chamada de `logSistemaService` (EmailService, AuthController,
PncpSyncService) têm o mesmo problema: a `categoria` ajuda um pouco (ex.
`CONVITE`, `LICITACAO_SYNC`), mas não há método/classe nem, para erros HTTP,
qual endpoint foi acessado.

### Proposta de solução

**a) Capturar a origem (classe + método) automaticamente, sem mudar todo
call site.** Dá para adicionar um parâmetro opcional `origem` nos métodos de
`LogSistemaService` (`info`/`aviso`/`erro`) que, quando não informado, é
preenchido sozinho a partir do stack trace de quem chamou
(`new Throwable().getStackTrace()[1]` → nome da classe + método). Isso é um
padrão comum em bibliotecas de log (é como o `java.util.logging` descobre
"quem chamou" sem o chamador precisar passar nada) — zero esforço nos ~10
call sites já existentes, e todo call site novo já ganha de graça.

**b) Capturar o endpoint quando o erro vem de uma requisição HTTP.** O
`@ExceptionHandler` do Spring já pode receber `HttpServletRequest` como
parâmetro do método — então dá pra montar `"POST /api/admin/licitacoes/sincronizar"`
direto do método/URI da requisição, sem nenhuma infraestrutura nova. Para
erros que não vêm de HTTP (os `@Scheduled`, como `PncpSyncService` ou os
e-mails diários), esse campo simplesmente fica nulo — não é um erro, é
contexto que não se aplica.

**c) Guardar os dois campos na tabela, mostrar na tela.**

```sql
alter table log_sistema add column origem varchar(255);   -- "PncpSyncService.sincronizarUf"
alter table log_sistema add column endpoint varchar(255); -- "POST /api/admin/licitacoes/sincronizar"
```

Na tela de Logs, isso vira só mais uma linha dentro do modal de detalhe que
já existe (o que abre com o ícone de "olho") — não precisa de UI nova, só
mostrar os dois campos quando não forem nulos.

**d) Opcional, mais caro — stack trace completo guardado no banco.** Daria
para ver a pilha de chamadas inteira na tela, sem precisar olhar o terminal.
Tem um custo real: linhas de log ficam bem maiores (o cleanup semanal já
existe e ajuda, mas o texto de um stack trace Java pode ter várias KB). Eu
sugeriria deixar isso de fora por ora — os itens (a)-(c) já resolvem o
pedido (endpoint, service e função) sem esse custo; se no dia a dia (a)-(c)
não forem suficientes para depurar um problema, aí vale reconsiderar.

**Escopo**: é uma mudança pequena e isolada (uma migration, um parâmetro
opcional no service, um ajuste no exception handler, uma linha no modal do
front) — dá pra fazer rápido se você quiser seguir com isso.

## 2. Tratamento de erros — outros pontos

- **Só o 500 genérico loga em `log_sistema`.** Erros 404/400/401/403/409 (ex.
  tentar excluir algo que não existe, token inválido, acesso sem permissão)
  não geram nenhuma linha de log hoje — eles só voltam uma resposta HTTP pro
  front. Isso é intencional em parte (não faz sentido logar "AVISO" para todo
  404), mas vale decidir deliberadamente: hoje, se um admin começar a receber
  403 do nada (como já aconteceu nesta sessão, por JWT expirado), não existe
  nenhum rastro disso na tela de Logs — só no console do navegador.
- **Sem ID de correlação.** Se o mesmo erro aparece em várias linhas de log
  (ex. uma rotina que roda para várias UFs), não tem como saber hoje que são
  do mesmo disparo/execução. Um `UUID` gerado por requisição/execução
  agendada, propagado para todas as linhas daquela execução, resolveria —
  mas é um item de "nice to have", não crítico no volume atual.

## 3. Autenticação / sessão

- **Token expira em 60 min, sem refresh.** Isso já causou confusão real
  nesta sessão (um "403 Forbidden" que na verdade era sessão expirada, não
  bug). Hoje a única saída para o usuário é fazer login de novo. Um refresh
  token (ou simplesmente aumentar a expiração, já que é um admin interno de
  poucos usuários) resolveria o atrito sem grande risco de segurança nesse
  contexto.
- **Sem revogação de token** (já documentado como limitação conhecida no
  `backend/README.md`) — se um admin for desativado, qualquer JWT emitido
  antes continua válido até expirar. Baixo risco dado o tamanho da equipe,
  mas vale ter escrito.

## 4. Performance / escalabilidade (nada urgente, mas vale ter no radar)

- **Sem paginação em nenhuma lista.** `log_sistema` tem um teto fixo de 200
  linhas (`findTop200ByOrderByCriadoEmDesc`); Clientes, Vagas, Serviços,
  Depoimentos, Licitações retornam a lista inteira sempre. Com o volume atual
  (dezenas de linhas) isso é irrelevante — mas é o tipo de coisa que, se não
  for decidida agora, vira um retrabalho maior mais pra frente.
- **Bundle do admin em ~900KB** (aviso do Vite no build, não tratado) — um
  único chunk JS carregado de uma vez. Code-splitting por rota reduziria o
  tempo de carregamento inicial, mas não é crítico com poucos usuários
  internos.
- **Nenhuma auditoria de "quem mudou o quê".** Login e envio de e-mail já são
  logados, mas uma mudança de status de uma Licitação ou a exclusão de um
  Cliente não registra qual admin fez a ação. Se dois ou mais admins
  passarem a usar o sistema, isso pode importar.

## 5. Testes — lacunas já documentadas, reafirmadas aqui

- Backend: sem teste de camada HTTP (`@WebMvcTest`/`MockMvc`) para a maioria
  dos controllers — os 53 testes criados na reestruturação cobrem services e
  um controller de autorização, não o contrato HTTP completo de cada
  endpoint.
- Frontend admin: só `mascaras.ts` tem teste hoje — páginas (`ClientesPage`,
  `LicitacoesPage`, etc.) não têm teste de componente ainda (foi decisão
  explícita priorizar lógica de negócio crítica sobre cobertura exaustiva).
- Site (Astro): zero testes.

Nenhum desses é urgente isoladamente — são custo de manutenção futura, não
bugs ativos.

## 6. Resumo de prioridades técnicas

**P0** — baixo custo, resolve atrito real já sentido nesta sessão:
1. Enriquecer `log_sistema` com `origem` + `endpoint` (seção 1) — pedido
   direto do usuário, implementação pequena.
2. Aumentar expiração do JWT ou adicionar refresh token — evita confundir
   "sessão expirou" com "bug real" de novo.

**P1** — vale fazer antes de publicar em produção:
3. Decidir e documentar deliberadamente o que vira log além do 500 genérico.
4. Paginação nas listas que podem crescer (Logs, Clientes, Licitações).

**P2** — débito técnico, sem urgência:
5. Code-splitting do bundle do admin.
6. ID de correlação em execuções agendadas.
7. Auditoria de alterações (quem mudou o quê).
8. Ampliar cobertura de teste (`@WebMvcTest`, componentes do admin, site).

Como nos outros documentos de planejamento, nada aqui foi implementado —
é para você decidir o que entra e em que ordem.
