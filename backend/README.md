# Backend — API da MHR

Java 25 + Spring Boot 4, Gradle (Kotlin DSL), PostgreSQL. Serve o site público
(`/api/*`) e o painel admin (`/api/admin/*`, `/api/auth/*`).

## Rodando localmente

```bash
cd infra && docker compose up -d   # Postgres
cd backend && ./gradlew bootRun    # API em http://localhost:8080
```

Swagger UI: `http://localhost:8080/docs`. Variáveis de ambiente: ver
[`.env.example`](.env.example) — nenhuma é obrigatória para rodar localmente
(todas têm um valor padrão de desenvolvimento).

## Estrutura dos pacotes

Organização por **camada técnica** (não mais por feature): toda entidade mora em
`model/`, todo repository em `repository/`, e assim por diante — mais fácil achar
"todos os endpoints admin" ou "todas as entidades" de uma vez, ao custo de um
cadastro inteiro (ex.: Vaga) ficar espalhado em 6 pacotes diferentes.

| Pacote | O que tem |
|---|---|
| `model/` | Todas as `@Entity` (`Vaga`, `Servico`, `Depoimento`, `PostBlog`, `LeadContato`, `Candidatura`, `Usuario`, `TokenRedefinicaoSenha`, `Cliente`, `LogSistema`). |
| `repository/` | Todos os `JpaRepository`. |
| `dto/` | Todos os DTOs de resposta (`XDto`). |
| `dto/request/` | Todos os corpos de requisição (`SalvarXRequest`, `ConvidarUsuarioRequest`, `LoginRequest`...). |
| `mapper/` | Todos os `@Mapper` do MapStruct (`Entidade` ↔ `Dto`/`Request`). |
| `service/` | Toda a lógica de negócio — é aqui que ficam as regras (ex.: "vaga encerrada não aceita candidatura", "virou contrato exige cliente"), não nos controllers. |
| `security/` | `JwtService`, `JwtAuthenticationFilter`, `UsuarioDetailsService`. |
| `controller/` | Controllers públicos (`GET /api/vagas`, `POST /api/leads-contato`...). |
| `controller/admin/` | Controllers de `/api/admin/**` (protegidos por JWT). |
| `config/` | `SecurityConfig`, `CorsConfig`, `OpenApiConfig`, `SeedAdminRunner` (cria o admin padrão no primeiro boot). |
| `common/` | Exceção e `@RestControllerAdvice` compartilhados entre módulos. |

Cada entidade de conteúdo (Vaga, Serviço, Depoimento, Post do blog, Lead de
contato, Candidatura, Cliente) segue o mesmo padrão em camadas, usado como
referência para adicionar uma nova:

```
model/Entidade.java                      # @Entity, mapeia a tabela
repository/EntidadeRepository.java        # Spring Data JPA
dto/EntidadeDto.java                      # o que a API devolve (nunca a entidade direto)
dto/request/SalvarEntidadeRequest.java    # o que a API recebe para criar/editar
mapper/EntidadeMapper.java                # MapStruct: Entidade <-> Dto/Request
service/EntidadeService.java              # regras de negócio; controllers só delegam
controller/EntidadeController.java        # rotas públicas (só leitura do que está publicado)
controller/admin/EntidadeAdminController.java  # CRUD completo, sob /api/admin/**
```

## Banco de dados e migrations

Flyway, em `src/main/resources/db/migration/`. **Nunca edite uma migration já
aplicada** — crie uma nova (`V4__...sql`, etc.), mesmo para um ajuste pequeno;
é assim que o schema evolui em qualquer ambiente que já rodou uma versão anterior.

Nomes de tabelas/colunas em português (convenção do projeto — ver raiz do
monorepo). IDs são `uuid` nativo do Postgres, gerados em memória pela aplicação
via `@UuidGenerator` do Hibernate (não pelo `gen_random_uuid()` do banco, que só
cobre inserts feitos fora do JPA).

## Autenticação e autorização

- **Login**: `POST /api/auth/login` com e-mail/senha devolve um JWT (HMAC,
  expira em 8h por padrão — `app.jwt.expiration-minutes`/`JWT_EXPIRATION_MINUTES`,
  pensado pra cobrir um expediente sem exigir login de novo no meio do dia). O
  token carrega o e-mail (subject) e o papel (`ADMIN`/`EDITOR`) como claims.
- **Toda rota não listada como pública em `SecurityConfig` exige esse JWT**
  (`anyRequest().authenticated()` — nega por padrão, em vez de abrir por padrão).
- **Papel vs. endpoint**: `/api/admin/**` exige só estar logado (qualquer papel).
  Endpoints mais sensíveis usam `@PreAuthorize("hasRole('ADMIN')")` por cima disso
  — hoje só `UsuarioAdminController` (gestão de outros usuários). Para restringir
  um novo endpoint a ADMIN, é só adicionar a mesma anotação.
- **"Esqueci minha senha" e "aceitar convite" são o mesmo mecanismo**: um token de
  uso único (32 bytes aleatórios, só o hash SHA-256 fica no banco, expira em 30
  min — `TokenAcessoService`) que autoriza `POST /api/auth/redefinir-senha` a
  definir uma senha para aquele usuário. Um usuário convidado nasce com
  `senha_definida = false` e uma senha aleatória inutilizável; só consegue logar
  depois de usar esse token.
- **Desativar um usuário** (`ativo = false`) bloqueia o login imediatamente da
  próxima vez (via `UserDetails.disabled`), mas **não revoga um JWT já emitido**
  — ele continua valendo até expirar (no máximo `app.jwt.expiration-minutes`).
  Para revogação instantânea seria preciso uma blocklist de tokens (não
  implementado).
- **Acessar uma rota protegida sem um JWT válido** (ausente, inválido ou
  expirado) devolve 401 via `AutenticacaoNecessariaEntryPoint`, um
  `AuthenticationEntryPoint` próprio — esse caso é barrado na cadeia de
  filtros do Spring Security, antes de chegar ao Spring MVC, por isso não
  passa pelo `ApiExceptionHandler` comum. Fica registrado em "Logs"
  (categoria `AUTENTICACAO`), assim como um acesso negado por papel
  (`AccessDeniedException` de um `@PreAuthorize`, categoria `ACESSO`) e
  credenciais erradas no login.
- Sem `MAIL_USERNAME`/`MAIL_PASSWORD` configurados, os e-mails de convite/
  redefinição não são enviados de verdade — o link aparece só no log do backend
  (`EmailService`), para não travar o fluxo em desenvolvimento local. Hoje o
  projeto usa o **Resend** (SMTP) como provedor — ver `.env.example`.
- `EmailService` sempre tenta o envio real quando configurado, mas **nunca deixa
  isso quebrar o fluxo**: se o provedor recusar (ex.: destinatário fora do modo
  sandbox do Resend, sem domínio verificado), a falha vira um registro em
  "Logs" (categoria `CONVITE`/`REDEFINICAO_SENHA`) com o link, em vez de
  propagar a exceção. Só o endpoint de convite (`POST .../convidar`) expõe esse
  status ao chamador (`emailEnviado`) — "esqueci minha senha" não, de propósito
  (revelar isso permitiria descobrir quais e-mails têm cadastro).

## Licitações

Acompanhamento de licitações públicas (pregões, editais) que a MHR pode
participar, do achado até o resultado (`IDENTIFICADA → EM_ANALISE →
DOCUMENTACAO → PROPOSTA_ENVIADA → GANHOU/PERDEU/DESCARTADA`), com checklist de
documentos exigidos e responsável interno.

- **Cadastro manual** (`origem = MANUAL`): sempre disponível, qualquer
  licitação encontrada em qualquer portal.
- **Busca automática no PNCP** (`origem = PNCP_AUTOMATICO`,
  `PncpSyncService`): consulta `GET /v1/contratacoes/proposta` da API pública
  do Portal Nacional de Contratações Públicas (`pncp.gov.br/api/consulta`,
  sem necessidade de token) por UF (`app.licitacoes.ufs`) e filtra o resultado
  por palavra-chave no campo `objetoCompra` — **a API do PNCP não tem busca
  por palavra-chave nativa**, essa filtragem é feita pela aplicação. Dedupe
  por `numeroControlePNCP`.
- **Liga/desliga e palavras-chave, editáveis pelo admin**:
  `configuracao_licitacoes` guarda `busca_pncp_ativa` (**desligada por
  padrão**) e `palavras_chave` (lista separada por vírgula, seed inicial na
  migration `V9`), ambos editados direto na tela de Licitações
  (`GET`/`PATCH /api/admin/licitacoes/configuracao`) — não exige redeploy
  para ajustar a lista conforme o uso real mostrar falso-negativos. O
  `@Scheduled` diário (7h) e o botão "Buscar agora"
  (`POST /api/admin/licitacoes/sincronizar`) checam `busca_pncp_ativa` antes
  de qualquer chamada à API — desligado, nenhuma chamada externa acontece.
- **Rate limit real do PNCP**: testado em desenvolvimento, o PNCP devolve
  `429 Too Many Requests` (com uma página HTML, não JSON) sob uso mais
  intenso — não documentado no OpenAPI, mas real. `PncpSyncService` trata
  isso por UF (uma UF falhando não derruba as outras) e registra em "Logs"
  (categoria `LICITACAO_SYNC`) em vez de propagar erro 500.

## Alerta de mensagens pendentes

`NotificacaoLeadsService.alertarMensagensPendentes()` roda todo dia às 9h
(`@Scheduled`) e, se houver mensagens de contato com status `NOVO` há mais de
24h, manda um e-mail (via `EmailService.enviarAlertaMensagensPendentes`) para
todo usuário `ADMIN` ativo, com a quantidade e a data da mensagem mais antiga.
Continua avisando todo dia enquanto a mensagem não for atendida (status
mudado) — é um lembrete recorrente, não um aviso único. No admin, o mesmo
número aparece como selo ao lado de "Mensagens" na barra lateral e como
banner no Dashboard (`GET /api/admin/dashboard`, campo `leadsNovos`).

Mesmo padrão para licitações: `NotificacaoLicitacoesService` avisa todo ADMIN
ativo quando uma `Licitacao` tem `dataEncerramentoProposta` a menos de 2 dias
e ainda não está em `PROPOSTA_ENVIADA`/`GANHOU`/`PERDEU`/`DESCARTADA`.

## Logs do sistema

`log_sistema` guarda eventos recentes (login, envio de e-mail, erro inesperado,
acesso sem token válido ou sem o papel exigido) para a tela **Logs** do admin
(só ADMIN, `GET /api/admin/logs`, com `DELETE` por id ou geral) — é diagnóstico
rápido, não uma trilha de auditoria completa (não registra cada ação de CRUD).
`LogSistemaService.limparAntigos()` roda toda segunda às 3h (`@Scheduled`) e
apaga tudo com mais de 7 dias, para a tabela não crescer sem limite.

Cada evento guarda também **origem** (classe + método de quem chamou
`info`/`aviso`/`erro`, capturado automaticamente via stack trace — nenhum
call site precisa informar isso) e, quando o evento veio de uma requisição
HTTP, o **endpoint** (`POST /api/admin/vagas`, por exemplo). Só o 500 genérico
e os casos de 401/403 (ver "Autenticação e autorização" acima) geram log hoje
— um 404/400 comum não gera, de propósito, pra não encher a tela de ruído.

## Upload de imagens

`POST /api/admin/uploads` (multipart, campo `arquivo`) salva a imagem (JPEG/PNG/
WEBP/GIF, até 5MB) e devolve a URL absoluta. Usado em Depoimentos (foto), Blog
(capa) e Clientes (logo) — o campo de URL continua editável manualmente
também, para quem preferir colar um link já hospedado em outro lugar.

`UploadService` escolhe o destino sozinho, sem nenhuma flag/perfil:

- **Storage de objeto (Cloudflare R2, compatível com a API do S3)**, quando as
  variáveis `R2_ACCESS_KEY_ID`/`R2_SECRET_ACCESS_KEY`/`R2_BUCKET_NAME`/
  `R2_ENDPOINT`/`R2_PUBLIC_URL` estão configuradas (ver `.env.example`) — o
  bean `S3Client` (`ArmazenamentoR2Config`) só existe quando todas elas estão
  preenchidas; a classe retorna `null` nesse caso, e Spring trata isso como
  "nenhum bean registrado", então `UploadService` recebe `null` e cai no
  fallback abaixo. Sobrevive a um redeploy — é o modo esperado em produção.
- **Disco local** (`app.uploads.dir`, default `uploads/`, servido em
  `/uploads/**` via `WebConfig`) quando o R2 não está configurado — bom o
  bastante para desenvolvimento, mas **não sobrevive a um redeploy** na
  maioria dos free tiers (filesystem efêmero). É o fallback automático, não
  precisa de nenhuma conta na Cloudflare para rodar localmente.

Um `Cliente` só aparece em `/api/clientes-destaque` quando o admin marca
`exibirNoSite = true` (desligado por padrão) — nunca automaticamente, porque
o cadastro de cliente guarda CNPJ e contato, e a projeção pública
(`ClienteDestaqueDto`) é uma classe separada que só expõe nome e logo, de
propósito, para não arriscar vazar esses dados por um erro futuro de mapeamento.

## Anti-spam, observabilidade e auditoria

- **Cloudflare Turnstile**: `POST /api/leads-contato` e
  `POST /api/vagas/{id}/candidaturas` exigem um `turnstileToken` válido
  (`TurnstileService`, verificado contra `challenges.cloudflare.com`). Por
  padrão usa a chave secreta de teste da Cloudflare (sempre aprova, não
  precisa de conta) — troque `TURNSTILE_SECRET_KEY` por uma chave real antes
  de produção, em par com `PUBLIC_TURNSTILE_SITE_KEY` no site.
- **Correlação entre linhas de log**: rotinas agendadas (sincronização do
  PNCP, alertas diários de leads/licitações) marcam um `correlacaoId` comum
  (`LogSistemaService.iniciarCorrelacao`/`encerrarCorrelacao`, thread-local)
  em todas as linhas de log daquela execução — fica visível no modal de
  detalhe da tela de Logs.
- **Auditoria leve**: `AuditoriaAspect` (Spring AOP) loga automaticamente,
  categoria `AUDITORIA`, toda chamada `POST`/`PUT`/`PATCH`/`DELETE` bem
  sucedida em qualquer controller de `controller/admin/**` — quem fez e qual
  endpoint, sem precisar adicionar a chamada em cada service (cobre também
  controllers novos, automaticamente). Não é uma trilha completa (não grava
  o payload antes/depois), mesma limitação já documentada do `log_sistema`.
- **Paginação**: `GET /api/admin/clientes/paginado` (com `busca`/`page`/`size`)
  é usado pela tela principal de Clientes; `GET /api/admin/clientes` (lista
  completa, sem paginar) continua existindo à parte, para os seletores de
  cliente em Depoimentos e Mensagens de contato. Licitações não ganhou
  paginação de verdade (a tela principal depende da lista completa em
  memória para os contadores de cada aba de status) — tem só um teto de
  segurança (`findTop500`, mesmo padrão do `log_sistema`).

## Endpoints

Lista completa e testável em `/docs` (Swagger UI). Resumo:

| Método + rota | Público? | O que faz |
|---|---|---|
| `GET /api/vagas`, `/api/vagas/{id}` | sim | Vagas abertas |
| `POST /api/vagas/{id}/candidaturas` | sim | Candidatar-se |
| `GET /api/servicos`, `/api/servicos/{slug}` | sim | Serviços publicados |
| `GET /api/depoimentos` | sim | Depoimentos publicados |
| `GET /api/posts-blog`, `/api/posts-blog/{slug}` | sim | Posts publicados |
| `GET /api/equipe` | sim | Integrantes da equipe publicados (página Sobre) |
| `GET /api/faq` | sim | Perguntas frequentes publicadas (home) |
| `GET /api/clientes-destaque` | sim | Clientes marcados para aparecer na home (só nome + logo) |
| `POST /api/leads-contato` | sim | Formulário de contato |
| `POST /api/auth/login` \| `/esqueci-senha` \| `/redefinir-senha` | sim | Autenticação |
| `GET/POST/PUT/DELETE /api/admin/{vagas\|servicos\|depoimentos\|posts-blog\|equipe\|faq\|clientes}` | logado | CRUD completo |
| `POST /api/admin/uploads` | logado | Upload de imagem (ver "Upload de imagens" abaixo) |
| `GET /api/admin/candidaturas` (+`PATCH .../{id}/status`) | logado | Candidaturas recebidas por vaga, status do processo seletivo |
| `GET /api/admin/leads-contato` (+`PATCH .../status`, `.../cliente`) | logado | Gestão de mensagens de contato, incl. "virou contrato" |
| `GET /api/admin/dashboard`, `/api/admin/dashboard/graficos` | logado | Contagens e dados de gráfico da tela inicial |
| `GET/POST/PATCH /api/admin/usuarios` | **ADMIN** | Listar, convidar, ativar/desativar usuários |
| `GET /api/admin/logs` | **ADMIN** | Últimos eventos do sistema (ver "Logs do sistema") |
| `GET/POST/PUT/DELETE /api/admin/licitacoes` (+`/status`, `/responsavel`, `/documentos`) | logado | CRUD + funil de licitações (ver "Licitações") |
| `GET/PATCH /api/admin/licitacoes/configuracao`, `POST .../sincronizar` | logado | Liga/desliga e dispara a busca no PNCP |

## Segurança — o que foi revisado e o que fica documentado como limitação conhecida

Feito:
- `@Valid` (ex.: `@NotBlank`, `@Email`) não tinha handler próprio em
  `ApiExceptionHandler` e caía no catch-all genérico — toda falha de
  validação virava 500 "erro interno do servidor" em vez de um 400 com a
  mensagem do campo. Achado testando o formulário de contato ao vivo depois
  de adicionar o Turnstile; agora tem `@ExceptionHandler(MethodArgumentNotValidException.class)`
  dedicado, testado em `ApiExceptionHandlerTest`.
- Autorização por papel nos endpoints de gestão de usuários (antes, qualquer
  usuário logado — inclusive EDITOR — podia convidar/desativar outros).
- `anyRequest().authenticated()` como padrão (antes havia um `anyRequest().permitAll()`
  residual que deixava endpoints não listados abertos).
- Enumeração de e-mail evitada em login (mensagem genérica) e em "esqueci senha"
  (sempre "sucesso", exista ou não o e-mail).
- Senha mínima de 8 caracteres na redefinição/convite.
- `DataIntegrityViolationException` (ex.: e-mail duplicado em corrida) vira 409
  com mensagem decente, em vez de 500 com stacktrace.
- Candidatura valida que a vaga está `ABERTA` antes de aceitar (antes, aceitava
  candidatura em qualquer vaga existente, mesmo encerrada).
- `/actuator/health` não depende mais de SMTP configurado (indicador de mail
  desativado — sem isso, toda checagem de saúde falhava tentando logar no Gmail).
- Acesso sem token válido devolve 401 (era um 403 genérico do Spring Security,
  sem nenhum registro em "Logs" — ver "Autenticação e autorização" acima).

Limitações conhecidas (decisão consciente de escopo, não descuido):
- **Sem rate limiting** em `/api/auth/login`, `/esqueci-senha` ou nos formulários
  públicos (`/api/leads-contato`, `/api/vagas/*/candidaturas`) — um ambiente de
  produção real deveria colocar isso atrás de um proxy/CDN com rate limiting
  (Cloudflare, por exemplo) ou adicionar algo como Bucket4j.
- **Sem CAPTCHA** nos formulários públicos — Cloudflare Turnstile está planejado
  para a Fase 3 (ver `docs/PLANEJAMENTO.md`).
- **Sem revogação de JWT** antes da expiração (ver seção de autenticação acima).
- `JWT_SECRET`/`ADMIN_PASSWORD` default só servem para desenvolvimento local —
  **trocar antes de qualquer deploy real** (ver `.env.example`).

## Testes

```bash
./gradlew test   # precisa de Docker rodando (Testcontainers)
```

A maior parte é **unit test puro** (JUnit 5 + Mockito, `src/test/java/.../service/`),
sem subir Spring nem banco — mocka o repository e testa só a regra de negócio do
`service/`. Prioridade foi a lógica mais sensível: JWT, convite/redefinição de
senha, `EmailService` (inclui o fallback quando o provedor recusa o envio),
"virou contrato" exigir cliente, vaga encerrada recusar candidatura, agrupamento
do gráfico do dashboard, limpeza semanal do log. CRUDs simples (Vaga, Serviço,
Depoimento, Post do blog, Cliente) têm cobertura básica de criar/listar/excluir,
não exaustiva.

Uma exceção roda com o contexto completo (Testcontainers):
`UsuarioAdminControllerAuthorizationTest` sobe a aplicação de verdade (JWT +
filtro de segurança reais) para confirmar que `@PreAuthorize("hasRole('ADMIN')")`
realmente barra um EDITOR com 403 — esse tipo de garantia declarativa só se
valida com o filtro de segurança no ar, não dá para "mockar" o `@PreAuthorize`.

**Camada HTTP** (`src/test/java/.../controller/admin/`): todos os demais
controllers `/api/admin/**` têm teste próprio agora (mesmo padrão enxuto do
`ApiExceptionHandlerTest` — `MockMvcBuilders.standaloneSetup` + Mockito, sem
subir o Spring inteiro nem o banco), cobrindo o contrato HTTP real: mapeamento
de rota, serialização JSON, e principalmente a validação `@Valid` → 400 via
`ApiExceptionHandler` (o bug de verdade que motivou adicionar esse tipo de
teste, ver "Correções pós-Fase 5" no README raiz) em cada endpoint de
criação/atualização, não só no único caso que o `ApiExceptionHandlerTest`
original cobria.

**Lacunas conhecidas**: sem teste de repository/query customizada contra banco
real (só a entidade/service são testados, não a query JPQL gerada pelo Spring
Data); sem teste de carga/concorrência nos tokens de uso único.
