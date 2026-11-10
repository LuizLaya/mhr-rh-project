# Deploy em produção

> Guia passo a passo para publicar o MHR usando só serviços de plano gratuito,
> seguindo a topologia já decidida em [`PLANEJAMENTO.md`](PLANEJAMENTO.md):
> **Neon** (banco), **Render** (API) e **Cloudflare Pages** (site + admin).
> Sem domínio próprio por enquanto — cada serviço dá um subdomínio gratuito
> (ex.: `mhr-backend.onrender.com`, `mhr-site.pages.dev`), que dá pra trocar
> por um domínio de verdade depois sem precisar refazer o deploy.

Contas necessárias (todas gratuitas, sem cartão de crédito exigido no Render
nem no Cloudflare; Neon também não pede): [neon.com](https://neon.com),
[render.com](https://render.com), [dash.cloudflare.com](https://dash.cloudflare.com).

## 1. Banco de dados (Neon)

1. Crie uma conta em neon.com (pode entrar com GitHub) e um novo projeto
   (ex.: nome "mhr").
2. Na tela do projeto, copie a **connection string** (formato
   `postgresql://usuario:senha@host/mhr?sslmode=require`) — o Neon já cria o
   banco `neon` ou similar por padrão; pode usar esse nome mesmo, ou criar um
   banco `mhr` pela aba "Databases".
3. Guarde separadamente host, usuário, senha e nome do banco — o Render pede
   cada um desses como variável de ambiente diferente (ver próximo passo),
   não a connection string inteira.

Não precisa rodar nenhuma migration manualmente: o Flyway (`spring.flyway.enabled: true`)
já aplica todas as migrations de `backend/src/main/resources/db/migration/`
sozinho na primeira subida do backend.

## 2. Backend (Render)

O repo já tem `backend/Dockerfile` (testado localmente) e `render.yaml` na
raiz (Blueprint do Render, provisiona o serviço com as variáveis certas já
nomeadas, só faltando os valores).

1. Crie uma conta em render.com (pode entrar com GitHub).
2. **New** → **Blueprint** → conecte o repositório `mhr-rh` do GitHub → o
   Render detecta o `render.yaml` sozinho e mostra o serviço `mhr-backend`
   para revisar.
3. Antes de confirmar, preencha as variáveis marcadas como obrigatórias (as
   que não têm valor padrão no blueprint):
   - `DB_URL`: `jdbc:postgresql://<host-do-neon>/<nome-do-banco>?sslmode=require`
   - `DB_USER`, `DB_PASSWORD`: do Neon (passo 1)
   - `CORS_ALLOWED_ORIGINS`: deixe em branco por enquanto — volta aqui depois
     do passo 3 (Cloudflare Pages), quando já tiver as URLs do site/admin
   - `ADMIN_EMAIL`/`ADMIN_PASSWORD`: credenciais do primeiro admin (troque
     depois de logar, isso só cria a conta inicial)
   - `MAIL_PASSWORD`: a API key do Resend (resend.com/api-keys)
   - `MAIL_FROM`: `onboarding@resend.dev` por enquanto (sem domínio
     verificado no Resend, e-mail só chega pra própria conta Resend - modo
     sandbox; ver `backend/.env.example`)
   - `FRONTEND_ADMIN_URL`: deixe em branco por enquanto, mesma razão do CORS
   - `TURNSTILE_SECRET_KEY`: pode deixar a chave de teste por enquanto
     (`1x0000000000000000000000000000000AA`) ou já trocar por uma real (ver
     seção 5)
   - `R2_*`: deixe tudo em branco por enquanto (upload cai pro disco local,
     que nesse caso é efêmero - ver seção 5 pra resolver isso depois)
4. Confirme a criação. O Render builda a imagem Docker (demora uns 3-5min no
   plano free) e sobe o serviço. Quando terminar, você terá uma URL tipo
   `https://mhr-backend.onrender.com`.
5. Teste: `https://mhr-backend.onrender.com/actuator/health` deve devolver
   `{"status":"UP"}`. Se não: aba **Logs** do serviço no Render mostra o erro.

**Limitação conhecida do plano free do Render**: o serviço "dorme" depois de
15 minutos sem receber requisição, e a próxima requisição demora uns 30-50s
pra "acordar" (cold start) — normal, não é bug. Documentado também em
`PLANEJAMENTO.md`.

## 3. Site e admin (Cloudflare Pages)

Dois projetos separados (são dois builds/outputs diferentes), mesma conta.

1. Crie uma conta em dash.cloudflare.com (pode entrar com GitHub).
2. **Workers & Pages** → **Create** → **Pages** → **Connect to Git** →
   selecione o repo `mhr-rh`.
3. **Site** (primeiro projeto):
   - Nome do projeto: `mhr-site` (vira `mhr-site.pages.dev`)
   - Framework preset: **Astro**
   - Build command: `npm run build`
   - Build output directory: `dist`
   - **Root directory** (em "Build configuration" → avançado): `site`
   - Variável de ambiente de build: `PUBLIC_API_BASE_URL` =
     `https://mhr-backend.onrender.com` (a URL do passo 2) e
     `PUBLIC_TURNSTILE_SITE_KEY` = a chave de teste
     (`1x00000000000000000000AA`) ou a real (seção 5)
4. **Admin** (segundo projeto, repita o fluxo):
   - Nome do projeto: `mhr-admin` (vira `mhr-admin.pages.dev`)
   - Framework preset: **Vite**
   - Build command: `npm run build`
   - Build output directory: `dist`
   - Root directory: `admin`
   - Variável de ambiente de build: `VITE_API_BASE_URL` =
     `https://mhr-backend.onrender.com`
5. Cada projeto builda e publica sozinho (alguns minutos). Acesse
   `https://mhr-site.pages.dev` e `https://mhr-admin.pages.dev` pra conferir.

Qualquer novo `git push` pra `master` depois disso faz o Cloudflare Pages
rebuildar e publicar sozinho (e o Render faz o mesmo pro backend) - não tem
passo manual de deploy no dia a dia, só nessa configuração inicial.

## 4. Conectar os três (CORS)

Com as URLs reais do site e do admin em mãos (passo 3), volte no Render
(serviço `mhr-backend` → **Environment**) e preencha:

- `CORS_ALLOWED_ORIGINS` = `https://mhr-site.pages.dev,https://mhr-admin.pages.dev`
- `FRONTEND_ADMIN_URL` = `https://mhr-admin.pages.dev` (usado no link de
  "esqueci minha senha"/convite enviado por e-mail)

Salvar essas variáveis já dispara um redeploy automático do backend.

## 5. Antes de usar com dados reais (recomendado, não bloqueia o deploy)

- **Cloudflare Turnstile de verdade**: dashboard da Cloudflare → **Turnstile**
  → criar um site, domínio `mhr-site.pages.dev` → copiar a site key (vai em
  `PUBLIC_TURNSTILE_SITE_KEY` no Pages) e a secret key (vai em
  `TURNSTILE_SECRET_KEY` no Render). Sem isso, o anti-spam dos formulários
  públicos não bloqueia nada de verdade (a chave de teste sempre aprova).
- **Upload de imagem com storage real**: criar um bucket no Cloudflare R2
  (dashboard → **R2** → **Create bucket**), gerar um token de API
  ("Manage R2 API Tokens") e preencher `R2_ACCESS_KEY_ID`/
  `R2_SECRET_ACCESS_KEY`/`R2_BUCKET_NAME`/`R2_ENDPOINT`/`R2_PUBLIC_URL` no
  Render (ver `backend/.env.example` pra onde achar cada valor). Sem isso, o
  upload cai pro disco local do Render, que **não sobrevive** a um redeploy.
- **Domínio verificado no Resend**: sem um domínio próprio ainda, o e-mail
  continua em modo sandbox (só chega pra própria conta Resend) - não
  bloqueia o deploy, só o convite de novos usuários/redefinição de senha por
  e-mail funcionando pra qualquer destinatário.
- **Trocar `JWT_SECRET`/`ADMIN_PASSWORD`**: o blueprint já gera um
  `JWT_SECRET` aleatório forte sozinho (`generateValue: true`); troque
  `ADMIN_PASSWORD` pelo painel assim que logar a primeira vez.

## Checklist rápido

- [ ] Neon: projeto criado, connection string em mãos
- [ ] Render: `mhr-backend` no ar, `/actuator/health` responde `UP`
- [ ] Cloudflare Pages: `mhr-site` e `mhr-admin` no ar
- [ ] `CORS_ALLOWED_ORIGINS`/`FRONTEND_ADMIN_URL` apontando pras URLs reais
- [ ] Login funciona no admin publicado, contra o backend publicado
- [ ] (opcional) Turnstile real, R2 real, domínio verificado no Resend
