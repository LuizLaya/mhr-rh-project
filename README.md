# MHR — Site institucional + portal de vagas

Monorepo do projeto MHR (RH). Planejamento completo em [docs/PLANEJAMENTO.md](docs/PLANEJAMENTO.md).

## Estrutura

```
backend/   API Java 25 + Spring Boot 4 (Gradle, Kotlin DSL) — ver backend/README.md
site/      Site público em Astro + ilhas React — ver site/README.md
admin/     Painel administrativo em React SPA (Vite) — ver admin/README.md
infra/     docker-compose para o Postgres local
docs/      Planejamento e outras decisões do projeto
```

Cada pasta tem seu próprio README com detalhes de arquitetura, convenções e o
que ainda falta: [backend](backend/README.md), [site](site/README.md),
[admin](admin/README.md). Este README cobre só o que é comum aos três.

## Pré-requisitos

- **JDK 25** (ex.: [Eclipse Temurin](https://adoptium.net/temurin/releases/?version=25)) — para o `backend/`
- **Node.js (LTS)** — para `site/` e `admin/`
- **Docker Desktop** — para rodar o Postgres local via `infra/docker-compose.yml`

Depois de instalar, feche e reabra o terminal para o `PATH` ser atualizado.

## Rodando tudo localmente

```bash
cd infra && docker compose up -d        # 1. Postgres
cd backend && ./gradlew bootRun         # 2. API em :8080 (ver backend/README.md)
cd site && npm install && npm run dev   # 3. Site em :4321 (ver site/README.md)
cd admin && npm install && npm run dev  # 4. Admin em :5173 (ver admin/README.md)
```

Cada subprojeto tem seu próprio `.env.example` — copie para `.env` antes do
`npm install`/primeiro `bootRun`.

## Deploy em produção

Guia passo a passo (Neon + Render + Cloudflare Pages, só serviços gratuitos)
em [docs/DEPLOY.md](docs/DEPLOY.md). `backend/Dockerfile` e `render.yaml`
(raiz do repo) já estão prontos e testados localmente.

## Convenção de nomenclatura

Tabelas, entidades, DTOs e campos de domínio ficam em **português** (`Vaga`,
`titulo`, `modelo_trabalho`...). Sufixos técnicos — `Controller`, `Repository`,
`Service`, `Config`, `Exception`, `DTO`/`Dto`, `Mapper` — continuam em inglês,
por serem convenção da própria linguagem/framework, não vocabulário de negócio.

## Estado atual

### Fase 0 — fundação ✅
Monorepo, Astro + Tailwind no site, layout base de todas as páginas públicas.

### Fase 1 — backend completo ✅
CRUD completo (público + admin) para `Vaga`, `Servico`, `Depoimento`,
`PostBlog`, `LeadContato`, `Candidatura`. Autenticação JWT real, com:
- Login, "esqueci minha senha" e **convite de novos usuários do painel** (um
  ADMIN convida por e-mail; a pessoa define a própria senha para ativar —
  detalhes em [backend/README.md](backend/README.md#autenticação-e-autorização));
- Autorização por papel (`ADMIN` vs `EDITOR`) na gestão de usuários;
- Dashboard (`GET /api/admin/dashboard`) com números agregados para a tela
  inicial do painel.

Site com conteúdo institucional real (Sobre, Serviços, Política de Privacidade,
Contato) e consumindo a API ao vivo (serviços, depoimentos, vagas, blog).

### Fase 2 — e-mail real, observabilidade e CRM básico ✅
- E-mail transacional de verdade via **Resend** (SMTP), com fallback automático
  para log quando o envio falha (ex.: destinatário fora do modo sandbox sem
  domínio verificado) — o admin vê na hora se o convite saiu ou não.
- **Logs do sistema** (`/logs`, só ADMIN): login, envio de e-mail e erros
  inesperados, com limpeza automática semanal (ver
  [backend/README.md](backend/README.md#logs-do-sistema)).
- **Clientes** (`/clientes`): empresas com contrato fechado, vinculáveis a
  Depoimentos e a Mensagens de contato marcadas como "virou contrato".
- Dashboard com gráficos (Recharts): leads/candidaturas por dia, conteúdo por
  tipo, mensagens e vagas por status.
- Admin com navegação lateral (sidebar) agrupada e tela de login redesenhada.

### Fase 3 — reestruturação, testes, Licitações e candidatura de ponta a ponta ✅
- Backend reorganizado de pacote-por-funcionalidade para **pacote-por-camada**
  (`model/repository/dto/mapper/service/controller`), com testes unitários e de
  integração que não existiam antes (ver [backend/README.md](backend/README.md#testes)).
- **Licitações** (`/licitacoes`): funil manual + busca automática no PNCP, com
  toggle liga/desliga e **palavras-chave editáveis direto no admin** (ver
  [backend/README.md](backend/README.md#licitações)).
- **Candidatura de ponta a ponta**: página de vaga no site
  (`/vagas/[slug]`) com formulário público de candidatura, e tela
  **Candidaturas** (`/candidaturas`) no admin para acompanhar o status de cada
  candidato (Nova/Em análise/Aprovado/Rejeitado) — antes a vaga era só vitrine,
  sem jeito de se candidatar.
- **Logs enriquecidos**: cada evento guarda a origem (classe + método,
  capturado automaticamente) e o endpoint HTTP envolvido; 401 (sem token
  válido) e 403 (sem o papel exigido) agora também geram log — antes só o 500
  genérico gerava um registro.
- Alertas por e-mail: mensagens de contato paradas há mais de 1 dia e
  licitações com prazo a menos de 2 dias.
- Edição (não só criar/excluir) em Vagas, Serviços, Depoimentos e Blog; busca
  por texto nessas telas e em Clientes.
- Máscaras de CNPJ/telefone e validação de e-mail nos formulários (site e
  admin); ícone de mostrar/ocultar e confirmação de senha nas telas de
  login/redefinição/convite.
- Sessão de expiração do JWT ampliada (8h) para não exigir login de novo no
  meio do expediente.

### Fase 4 — upload de imagem, Equipe, FAQ e clientes em destaque ✅
- **Upload de imagem de verdade** (`POST /api/admin/uploads`): foto de
  depoimento, capa de post e logo de cliente podem ser enviados direto do
  computador (disco local, ver [backend/README.md](backend/README.md#upload-de-imagens))
  — o campo de URL continua existindo, para quem preferir colar um link.
- **Equipe** (`/equipe`, página Sobre do site) e **FAQ** (`/faq`, home do
  site): dois módulos novos de conteúdo, mesmo padrão de CRUD + publicado/
  ordem dos demais.
- **Clientes em destaque na home**: um cliente só aparece publicamente quando
  o admin marca "mostrar no site" (desligado por padrão) — expõe só nome e
  logo, nunca CNPJ/contato, via uma projeção separada (`ClienteDestaqueDto`).
- Edição de Clientes (o único módulo de conteúdo que ainda não tinha).
- Tela **Versão** (`/versao`, aba Sistema): versão do painel/API, build,
  Java, Spring Boot e esquema do banco.

### Fase 5 — anti-spam, paginação, auditoria e débito técnico ✅
- **Cloudflare Turnstile** nos formulários públicos de contato e candidatura
  — chave de teste da Cloudflare por padrão, troque por uma real antes de
  produção (ver [backend/README.md](backend/README.md#anti-spam-observabilidade-e-auditoria)).
- **Correção de bug real**: `@Valid` (ex.: e-mail inválido, campo em branco)
  virava 500 "erro interno" em vez de 400 com a mensagem do campo — achado
  testando o Turnstile ao vivo, agora com handler próprio e teste de
  regressão.
- **Paginação** de verdade em Clientes (busca + página, servidor); Licitações
  ganhou um teto de segurança (não paginação completa — a tela depende da
  lista inteira para os contadores por status).
- **Auditoria leve**: todo `POST`/`PUT`/`PATCH`/`DELETE` num controller admin
  fica registrado (quem fez, o quê) via um aspecto — cobre automaticamente
  controllers novos, sem precisar adicionar a chamada em cada service.
- **Correlação de log**: rotinas agendadas (PNCP, alertas diários) marcam um
  id comum em todas as suas linhas de log, visível na tela de Logs.
- Admin: code-splitting por rota (`React.lazy`) — cada tela baixa só quando
  visitada, em vez de um bundle único.
- Mais testes automatizados: `CandidaturasPage`/`ClientesPage` no admin, e um
  teste de camada HTTP real (`ApiExceptionHandlerTest`) no backend.

### Correções pós-Fase 5 ✅
- **Blog do site**: a página `/blog` ficou anos como um placeholder estático
  (`TODO: buscar de GET /api/posts-blog`) mesmo com o CRUD completo existindo
  desde a Fase 1 — agora lista os posts publicados e tem página de detalhe
  (`/blog/[slug]`), no mesmo padrão de Serviços/Vagas.
- **Bug real no Turnstile**: o widget renderizava via `class="cf-turnstile"`
  (modo "implícito", escaneado pelo próprio script da Cloudflare), que mutava
  a div antes do React terminar de hidratar a ilha do formulário — o React
  descartava a árvore inteira ao notar a divergência, deixando "Enviar
  candidatura"/"Enviar mensagem" travado mesmo com o desafio resolvido.
  Trocado para renderização imperativa (`turnstile.render` num `useEffect`),
  sem conflito de hidratação (ver [site/README.md](site/README.md)).
- Visual da home: logos de clientes paravam de preto e branco (`grayscale`
  forçado) e sem padronização de tamanho; FAQ ganhou indicador visual
  (seta) de aberto/fechado.

### Fase 6 — storage de imagem real e cobertura de teste ✅
- **Upload de imagem com storage real**: `UploadService` usa Cloudflare R2
  (compatível com a API do S3) quando configurado (`R2_ACCESS_KEY_ID` e
  demais variáveis, ver `backend/.env.example`), sobrevivendo a um redeploy —
  antes só disco local, que não sobrevive em free tiers. Sem configurar,
  continua caindo pro disco local automaticamente (nenhuma conta na
  Cloudflare é necessária para desenvolvimento) — ver
  [backend/README.md](backend/README.md#upload-de-imagens).
- **Testes de camada HTTP no backend**: todos os controllers `/api/admin/**`
  que só tinham teste de service ganharam teste do contrato HTTP real
  (mapeamento de rota, `@Valid` → 400, serialização JSON) — ver
  [backend/README.md](backend/README.md#testes).
- **Testes automatizados no site**: o site público (Astro) tinha zero testes
  — agora cobre as funções puras (`mascaras.ts`, `zod-pt-br.ts`), os wrappers
  de fetch (`lib/api.ts`) e, mais importante, trava o fix de hidratação do
  Turnstile (`CampoTurnstile.test.tsx`) e o fluxo do formulário de contato
  (`ContactForm.test.tsx`) — ver [site/README.md](site/README.md#testes).

### O que falta (conhecido, documentado em detalhe nos READMEs de cada parte e em
[docs/ANALISE_PRODUTO_ROADMAP.md](docs/ANALISE_PRODUTO_ROADMAP.md) / [docs/ANALISE_TECNICA.md](docs/ANALISE_TECNICA.md))
- Produção: `backend/Dockerfile` + `render.yaml` prontos e testados
  localmente, guia completo em [docs/DEPLOY.md](docs/DEPLOY.md) — falta só
  executar os passos (contas em Neon/Render/Cloudflare são do usuário, não
  dá pra automatizar). Sem domínio verificado no Resend, e-mail só chega
  para a própria conta Resend (modo sandbox). Sem um bucket R2 configurado
  também, o upload de imagem continua em disco local (ver Fase 6 acima) —
  nesse caso **não sobrevive a um redeploy no Render**, diferente do
  disco local em desenvolvimento.
- Backend: sem teste de repository/query customizada contra banco real (só a
  entidade/service são testados, não a query JPQL gerada pelo Spring Data).
- Site: `CandidaturaForm.tsx` e as páginas `.astro` ainda não têm teste
  (mesmo padrão de risco baixo documentado no `site/README.md`).

## Status de build (local, após instalar JDK 25 + Node LTS + Docker)

- ✅ `backend`: `./gradlew build -x test` compila
- ✅ `site`: `npm run build` gera todas as páginas
- ✅ `admin`: `npm run build` compila sem erros
