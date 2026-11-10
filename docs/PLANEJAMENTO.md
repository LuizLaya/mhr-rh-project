# MHR — Planejamento do Projeto

> Site institucional + portal de vagas para consultoria de RH, inspirado em [reverh.com.br](https://reverh.com.br/), 100% com serviços gratuitos.

Marca: **MHR — "Pessoas certas. Resultados certos."** (azul-marinho `#102A43` + dourado `#B8893A`, conforme logos em `logoMHR/`).

---

## 1. Referência: o que o reverh.com.br tem

Análise do site (Nov/2026):

- **Home**: hero + CTA, "diferenciais" por unidade (Taguatinga/Gama), contador de números (anos de experiência, colaboradores impactados, vagas trabalhadas, projetos entregues), marcas/clientes em carrossel, seção "Quem somos", cards de serviços, depoimentos, FAQ (empresa x candidato), CTA final de contato.
- **Sobre** (`/a-reve`): história, propósito.
- **Serviços** (`#servicos` + páginas próprias): Consultoria de RH Estratégico, Recrutamento e Seleção, Treinamento e Desenvolvimento de Lideranças, Adequação à NR-01.
- **Blog**: conteúdo de SEO/autoridade.
- **Vagas**: **terceirizado** para a plataforma Empregare (ATS externo) — não é construído por eles.
- **Contato**: formulário + botão flutuante do WhatsApp.
- Banner de cookies (LGPD), política de privacidade.

**Decisão de escopo para o MHR**: replicar a estrutura institucional (Home, Sobre, Serviços, Blog, Depoimentos, FAQ, Contato, LGPD) e, diferente do site de referência, **construir o módulo de Vagas internamente** (lista de vagas + candidatura com upload de currículo + painel admin), já que é um projeto para aprendizado/portfólio e acrescenta valor real de RH.

---

## 2. Funcionalidades do site

### Público (site institucional)
1. **Home** — hero, diferenciais, contadores animados, logos de clientes, serviços em destaque, depoimentos, FAQ, CTA de contato.
2. **Sobre** — missão/visão/valores, história, equipe.
3. **Serviços** — listagem + página de detalhe por serviço (conteúdo customizável via admin).
4. **Vagas** — listagem com busca/filtro (cidade, área, modelo de trabalho), página de detalhe, formulário de candidatura com upload de currículo (PDF).
5. **Blog** — listagem, categorias, página de artigo (SEO: meta tags, sitemap.xml, dados estruturados).
6. **Depoimentos** — carrossel/listagem.
7. **Contato** — formulário (nome, e-mail, empresa, mensagem) + link direto de WhatsApp.
8. **Política de Privacidade / LGPD** — texto legal + banner de consentimento de cookies.

### Administrativo (painel interno, autenticado)
- Login (JWT) para usuários `ADMIN`/`EDITOR`.
- CRUD de Vagas, Posts do Blog, Depoimentos, Serviços.
- Lista de candidaturas recebidas por vaga + download de currículo.
- Lista de leads/mensagens de contato.
- Dashboard simples (nº de vagas ativas, candidaturas no mês, mensagens não lidas).

### Não funcionais
- **LGPD**: consentimento explícito para currículos/dados pessoais, política de retenção, dados sensíveis nunca em logs.
- **SEO**: SSR/pré-render das páginas públicas (ver seção de stack), sitemap, Open Graph.
- **Responsivo** (mobile-first) e **acessível** (WCAG AA básico).
- **Anti-spam**: Cloudflare Turnstile nos formulários de contato/candidatura.
- **Performance**: Lighthouse ≥ 90 em todas as categorias.

---

## 3. Stack técnica (tudo atual, 2025/2026)

### Backend
| Item | Escolha | Por quê |
|---|---|---|
| Linguagem | **Java 25 (LTS, set/2025)** | LTS mais recente; virtual threads (Loom) estáveis, compact source files, records |
| Framework | **Spring Boot 3.5+** | padrão de mercado, maduro, ótima doc |
| Persistência | **Spring Data JPA + Hibernate** sobre **PostgreSQL** | relacional, bom para vagas/candidatos/leads |
| Migrações | **Flyway** | versionamento do schema |
| Segurança | **Spring Security + JWT** | login do admin, rotas protegidas |
| Mapeamento DTO | **MapStruct** | evita boilerplate manual |
| Documentação API | **springdoc-openapi (Swagger UI)** | contrato vivo da API |
| Build | **Gradle (Kotlin DSL)** | mais rápido e moderno que Maven |
| Upload de arquivos | integração com **Cloudflare R2** (S3-compatible) | armazenar currículos/imagens fora do servidor |
| Testes | JUnit 5 + Testcontainers (Postgres real em CI) | testes confiáveis |

### Frontend
| Item | Escolha | Por quê |
|---|---|---|
| Lib | **React 19** | mais recente |
| Build tool | **Vite** | dev server rápido |
| Linguagem | **TypeScript** | tipagem |
| Estilo | **Tailwind CSS v4 + shadcn/ui (Radix)** | produtividade + acessibilidade pronta |
| Dados/cache | **TanStack Query** | chamadas à API com cache |
| Formulários | **React Hook Form + Zod** | validação robusta (candidatura, contato) |
| Rotas | **Astro file-based routing** (site público) + **React Router v7** (painel admin, SPA à parte) | decisão tomada: ver nota abaixo |

> **Decisão de SEO (confirmada)**: o site público (Home, Sobre, Serviços, Blog, Vagas) será construído em **Astro** — páginas estáticas/SSG, rápidas e 100% indexáveis — com **React "ilhas"** (`client:load`/`client:visible`) para as partes interativas: formulário de candidatura, filtro de vagas, formulário de contato com Turnstile, carrossel de depoimentos. O **painel admin** (autenticado, sem necessidade de SEO) é uma SPA React separada, consumindo a mesma API Spring Boot. Isso dá o melhor dos dois mundos: SEO forte para quem busca "consultoria de RH" ou vagas no Google, e React onde há interatividade real.

### Infraestrutura — tudo gratuito
| Camada | Serviço | Free tier | Observação |
|---|---|---|---|
| Frontend (estático) | **Cloudflare Pages** | ilimitado, CDN global, sem "sleep" | build automático a partir do GitHub |
| Backend (API) | **Oracle Cloud "Always Free"** (VM Ampere A1) *recomendado* | VM sempre ativa (sem cold start), specs generosas (variam, confirmar no console) | exige cartão de crédito no cadastro (não cobra), mais trabalho de DevOps (Docker + Nginx/Caddy + SSL) |
| Backend (alternativa simples) | **Render free** | fácil de configurar, mas "dorme" após 15 min de inatividade (cold start ~30s) e RAM apertada (256–512MB) | bom para MVP/testes, não ideal para produção |
| Banco de dados | **Neon (PostgreSQL)** | sempre gratuito, 0.5GB, auto-suspend | serverless, ótimo para o volume de um site institucional |
| Armazenamento de arquivos | **Cloudflare R2** | 10GB grátis, sem custo de saída (egress) | currículos PDF, imagens do blog |
| E-mail transacional | **Resend** ou **Brevo** | 3.000/mês ou 300/dia | notificação de novo contato/candidatura |
| Anti-spam | **Cloudflare Turnstile** | gratuito | captcha invisível |
| Monitoramento | **UptimeRobot** | gratuito | ping de disponibilidade |
| CI/CD | **GitHub Actions** | gratuito (repo privado grátis) | build/test/deploy automático |
| Domínio | **registro.br (.com.br)** | **não é gratuito** (~R$40/ano) | único custo real; pode começar em subdomínio grátis (`*.pages.dev`) e migrar depois |

**Recomendação de topologia**: começar com **Render free** (zero DevOps, no ar em minutos) para validar o projeto; quando o site estiver estável, migrar o backend para a **VM Oracle Always Free** (elimina cold start, melhor para produção de verdade). Ambos usam o mesmo banco Neon, então a migração é só do container da API.

---

## 4. Modelagem inicial de dados (alto nível)

> Nomenclatura do projeto: tabelas, entidades e campos de domínio em português
> (sufixos técnicos como `Controller`/`Repository`/`DTO` continuam em inglês, por
> serem convenção da própria linguagem/framework).

- `Usuario` (tabela `usuarios`; admin/editor — login do painel) — **implementado** (seed automático + JWT)
- `Vaga` (tabela `vagas`; título, descrição, cidade, modelo de trabalho, status, datas) — **implementado** (CRUD completo)
- `Candidatura` (tabela `candidaturas`; vaga, nome, e-mail, telefone, currículo (URL, upload real no R2 fica para a Fase 3), consentimento LGPD) — **implementado**
- `PostBlog` (tabela `posts_blog`; título, slug, conteúdo, capa, categoria, publicado) — **implementado**
- `Depoimento` (tabela `depoimentos`; nome, empresa, cargo, texto, foto) — **implementado**
- `Servico` (tabela `servicos`; título, descrição, ícone) — **implementado**
- `LeadContato` (tabela `leads_contato`; nome, e-mail, empresa, mensagem, status) — **implementado**

---

## 5. Roadmap de execução (solo, part-time — ~8 a 10 semanas)

| Fase | Duração | Entregas |
|---|---|---|
| **0. Fundação** | ~1 sem. | Repo (monorepo `backend/` + `frontend/`), estrutura de pastas, CI básico, paleta/design tokens extraídos da logo |
| **1. Backend base** | ~1,5 sem. | ✅ **Concluído** — Projeto Spring Boot, entidades + Flyway, Spring Security/JWT, Swagger, CRUD de Vagas/Blog/Depoimentos/Serviços/Leads/Candidaturas |
| **2. Frontend base** | ~1,5 sem. | Scaffold React/Astro, layout (header/footer), tema Tailwind com cores da marca, páginas estáticas (Home, Sobre, Política de Privacidade) |
| **3. Features principais** | ~2,5 sem. | Vagas (lista+detalhe+candidatura com upload), Blog, Depoimentos, Contato (com Turnstile + e-mail), Painel Admin (CRUD + candidaturas + leads) |
| **4. Qualidade** | ~1 sem. | SEO (sitemap, OG, meta), banner LGPD, acessibilidade, ajuste de performance (Lighthouse) |
| **5. Deploy** | ~1 sem. | Provisionar Neon/R2/Render (ou Oracle)/Cloudflare Pages, domínio + SSL, pipelines de deploy, monitoramento, backup do banco |
| **6. Pós-lançamento** | contínuo | Conteúdo do blog, analytics (ex.: Plausible — atenção à LGPD vs. Google Analytics), ajustes com base em uso real |

---

## 6. Próximos passos imediatos

1. ~~Confirmar Astro+React vs. React puro~~ — **decidido: Astro+React**.
2. Definir os serviços reais que a MHR vai oferecer (para substituir os 4 cards de exemplo do reverh).
3. Criar o repositório Git e a estrutura de pastas do monorepo.
4. Começar pela Fase 0.
