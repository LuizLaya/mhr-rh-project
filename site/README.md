# Site — mhr.com.br

Site público institucional. Astro (SSG — HTML estático de verdade) + ilhas React
onde há interação real (formulário de contato, candidatura a vaga). Consome a
API pública do [backend](../backend/README.md), nunca `/api/admin/**`.

## Rodando localmente

```bash
cp .env.example .env   # PUBLIC_API_BASE_URL=http://localhost:8080 por padrão
npm install
npm run dev             # http://localhost:4321
```

Precisa do backend rodando (`cd ../backend && ./gradlew bootRun`) para as
páginas que buscam dados (vagas, serviços, depoimentos, blog) mostrarem algo
além do estado vazio — elas não quebram sem o backend, só ficam sem conteúdo.

## Por que Astro (e não só React)?

Um site institucional depende de SEO — gente achando a MHR no Google. Uma SPA
React pura renderiza no cliente, o que buscadores indexam pior. Astro gera HTML
estático em build time (`output: "static"` em `astro.config.mjs`) e só carrega
JavaScript nas partes que realmente precisam de interatividade (`client:load`
em `ContactForm.tsx`, por exemplo) — o resto da página é HTML puro, rápido e
indexável. O [painel admin](../admin/README.md), que não precisa de SEO, é uma
SPA React de verdade.

## Páginas dinâmicas e o `output: "static"`

Como o site é gerado em build time, uma rota dinâmica (`/servicos/[slug]`,
`/vagas/[slug]`) **precisa saber todos os valores possíveis de `slug` durante
o build** — é o que `getStaticPaths()` em `src/pages/servicos/[slug].astro` e
`src/pages/vagas/[slug].astro` faz, buscando todos os serviços/vagas da API
antes de gerar as páginas. `/vagas/[slug]` mostra a descrição completa da vaga
e, se ela ainda estiver `ABERTA`, o formulário público de candidatura
(`components/react/CandidaturaForm.tsx`, que posta em
`POST /api/vagas/{id}/candidaturas`).

**Consequência prática**: um serviço novo cadastrado no admin só aparece no site
depois de um novo `npm run build` + deploy — não é instantâneo. Para o volume de
conteúdo institucional da MHR isso é aceitável (rebuild automático via webhook
do admin é uma melhoria futura, não implementada). Páginas que só *listam* algo
(`/vagas`, `/servicos`) não têm esse problema — são buscadas a cada build, só a
rota *de detalhe* por slug precisa do `getStaticPaths`.

## Estrutura

```
src/
  layouts/BaseLayout.astro   # <head>, Header, Footer, botão de WhatsApp
  components/                # Header, Footer, WhatsAppButton (.astro)
  components/react/          # ilhas React (ContactForm, CandidaturaForm, CampoTurnstile)
  lib/api.ts                 # funções buscarX() que chamam a API pública
  lib/contato.ts             # WhatsApp/e-mail/cidade da MHR, usado em 3 lugares
  lib/zod-pt-br.ts           # traduz mensagens de validação do Zod
  lib/mascaras.ts            # mascararTelefone ("enquanto digita")
  pages/                     # uma rota por arquivo (convenção do Astro)
```

`Header.astro` busca vagas e posts do blog em build time só para decidir se
mostra esses links no menu — se não há nenhum publicado, o link some (pedido
explícito: não mostrar uma seção vazia).

**Anti-spam (Cloudflare Turnstile)**: os formulários de contato e candidatura
exigem um token do Turnstile (`CampoTurnstile.tsx`, script carregado no
`BaseLayout`) antes de enviar — o backend verifica o token de verdade
(`TurnstileService`). `PUBLIC_TURNSTILE_SITE_KEY` vem com a chave pública de
teste da Cloudflare por padrão (funciona sem conta); troque por uma chave
real antes de produção. O widget é renderizado de forma **imperativa**
(`turnstile.render(container, ...)` num `useEffect`), não pela div
`class="cf-turnstile"` que o script escaneia sozinho — essa segunda forma
("implícita") deixava o script do Turnstile preencher a div antes do React
terminar de hidratar a ilha, e o React descartava a árvore inteira ao notar
a divergência entre o HTML do servidor (div vazia) e o DOM real, deixando o
botão de enviar travado mesmo com o desafio resolvido. O script é carregado
com `?render=explicit` no `BaseLayout` para garantir que só a nossa chamada
renderiza o widget.

## Testes

```bash
npm run test   # Vitest + Testing Library, não precisa do backend rodando
```

`vitest.config.ts` usa `getViteConfig` (helper oficial do Astro) para reaproveitar
o Vite de `astro.config.mjs` — sem isso, os testes de componente React (`.tsx`)
não teriam o JSX transformado. Cobertura: `lib/mascaras.ts` e `lib/zod-pt-br.ts`
(funções puras), `lib/api.ts` (wrappers de fetch, mockando `fetch` global),
`CampoTurnstile.tsx` (trava o contrato do fix de hidratação acima: nunca usa a
classe/atributos que o script da Cloudflare escaneia sozinho, chama
`turnstile.render` imperativamente e `turnstile.remove` ao desmontar) e
`ContactForm.tsx` (validação, botão só habilita com o Turnstile resolvido,
sucesso/erro do envio). `CandidaturaForm.tsx` e as páginas `.astro` ainda não
têm teste — mesmo padrão de `CampoTurnstile`/`ContactForm` cobre o risco
principal (o fix de hidratação), as páginas estáticas têm baixo risco de lógica.

## Conteúdo

Página **Sobre** e **Política de Privacidade** têm o texto principal fixo no
próprio `.astro` (editar o arquivo e rebuildar) — são páginas institucionais
que mudam raramente. **Serviços**, **Vagas**, **Depoimentos**, **Blog**,
**Equipe** (seção na página Sobre) e **FAQ**/**Clientes em destaque** (seções
na home) vêm do banco via API, editáveis pelo [painel admin](../admin/README.md)
sem precisar mexer em código (mas lembrando da limitação de rebuild acima) —
cada seção só aparece quando há pelo menos um item publicado/marcado, senão
some inteira (mesmo padrão do link de Vagas/Blog no menu).
