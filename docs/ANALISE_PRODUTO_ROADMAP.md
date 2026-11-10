# Análise de produto e roadmap — novembro/2026

> Levantamento do que existe hoje vs. o plano original (`PLANEJAMENTO.md`), e do
> que falta para o site + admin ficarem completos como ferramenta de trabalho da
> MHR. Documento de planejamento — nenhum item aqui foi implementado, é para
> revisão e priorização em conjunto.

## 1. Onde o projeto está vs. onde o `PLANEJAMENTO.md` original previa

O plano original guiou a Fase 1 (site institucional + admin básico), mas o
projeto cresceu bastante a partir de pedidos pontuais que não estavam lá:
Logs do sistema, Licitações (PNCP), Clientes, alertas automáticos por e-mail,
sidebar com ícones/colapso, máscaras de CNPJ/telefone. Por outro lado, algumas
coisas que o plano original previa para a Fase 1/2 **nunca foram construídas**:

| Previsto no plano original | Status real hoje |
|---|---|
| shadcn/ui no admin | Não usado — componentes próprios com Tailwind puro |
| Upload de imagem (Cloudflare R2) | Não existe — depoimentos/blog/serviços usam campo de **URL**, não upload de arquivo |
| Cloudflare Turnstile (anti-spam no formulário de contato) | Não existe — há um `TODO` deixado no código (`ContactForm.tsx`) |
| Deploy (Render/Oracle/Neon/Cloudflare Pages) | **Nada publicado ainda** — tudo roda só local |
| Seção "Equipe" / FAQ / carrossel de clientes na home | Não construídas |
| Analytics (Plausible) | Não instalado |

## 2. O maior gap de produto: a vaga é publicada, mas ninguém consegue se candidatar

Isso merece destaque porque é o tipo de lacuna que só aparece quando se olha o
fluxo de ponta a ponta: o backend já tem `Candidatura` completo (modelo,
endpoint público `POST /api/candidaturas`, vínculo com `Vaga`), e o admin já
lista vagas — mas:

- **Não existe página de detalhe da vaga no site** (`/vagas/[slug]`) — só a
  listagem em `/vagas`.
- **Não existe formulário público de candidatura** — ou seja, hoje é
  tecnicamente impossível um candidato real se candidatar pelo site.
- **Não existe tela no admin para ver quem se candidatou** a cada vaga —
  mesmo se alguém candidatasse via API diretamente, ninguém veria isso na
  interface.

Ou seja, o módulo de Vagas hoje é só uma vitrine, sem o fluxo que dá valor de
negócio (atrair e captar candidatos). Isso é o item de maior prioridade de
produto deste documento.

## 3. Lacunas de CRUD no admin (edição)

Hoje Vagas, Serviços, Depoimentos e Blog só têm **criar + listar + excluir** —
não há **editar**. Na prática, qualquer correção de texto/preço/data exige
excluir e recriar (perdendo o histórico/id). Clientes e Licitações já têm
edição; vale levar os outros módulos ao mesmo padrão.

## 4. O que cada módulo construído ad hoc ainda não cobre

- **Licitações**: a lista de palavras-chave do PNCP só é ajustável via
  variável de ambiente/`application.yml` (precisa de mim ou redeploy para
  mudar) — não há tela no admin para o próprio usuário ajustar as palavras.
  Também não há Kanban (arrastar card entre status), só filtro por aba.
- **Clientes**: o vínculo "depoimento/mensagem virou contrato" existe no
  banco, mas não há uma visão "por cliente, quais depoimentos e qual histórico
  de contato geraram esse contrato" — é só uma lista plana.
- **Logs**: cobre bem observabilidade técnica (ver análise técnica em
  `ANALISE_TECNICA.md`), mas não é uma lacuna de produto em si.

## 5. Busca e filtros

Fora de Logs, Leads e Licitações (que já têm filtro por status), nenhuma
tela do admin tem busca por texto — em Vagas, Serviços, Depoimentos, Blog e
Clientes, a única forma de achar algo é rolar a lista com os olhos. Como o
volume ainda é baixo isso não dói hoje, mas cresce com o uso.

## 6. O que falta para o site "vender" mais o institucional

Itens do pacote original de conteúdo que nunca saíram do papel: seção "Equipe"
(fotos/bios), FAQ, carrossel/lista de clientes atendidos (pode já puxar da
tabela `clientes` que existe!), contadores animados de estatísticas na home.
Esses são baratos de construir e têm efeito direto em conversão/credibilidade.

## 7. Prioridades sugeridas

**P0 — Bloqueia o objetivo do site de RH**
1. Página de detalhe da vaga (`/vagas/[slug]`) + formulário público de
   candidatura (com upload ou link de currículo) + tela de Candidaturas no
   admin (ver por vaga quem se aplicou, mudar status).
2. Publicar o site/admin em algum ambiente real (ainda que free tier) — sem
   isso, o e-mail via Resend continua travado em modo sandbox e nada do que
   foi construído é visível fora da máquina local.

**P1 — Fecha lacunas de uso diário**
3. Formulários de edição em Vagas, Serviços, Depoimentos, Blog (hoje só
   criar/excluir).
4. Tela de configuração das palavras-chave do PNCP no admin (sem precisar de
   redeploy).
5. Busca por texto nas listas do admin (Vagas, Serviços, Depoimentos, Blog,
   Clientes).

**P2 — Melhora institucional/conversão do site**
6. Seção "Equipe", FAQ, lista de clientes atendidos na home (dado já existe
   em `clientes`), contadores de estatística.
7. Upload de imagem real (hoje é só URL) para depoimentos/blog/serviços —
   decisão consciente de escopo no plano original, mas vale reavaliar agora
   que o resto do sistema está mais maduro.
8. Cloudflare Turnstile no formulário de contato público (hoje sem proteção
   anti-spam).

Nada disso foi implementado — é a lista para você priorizar. Minha sugestão,
se for escolher só uma coisa agora, é o item 1 (candidatura): é o único gap
que impede o site de cumprir a função mais básica dele.
