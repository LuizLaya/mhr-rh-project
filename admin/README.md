# Admin — painel administrativo da MHR

SPA em React 19 + Vite + React Router. Único consumidor de `/api/admin/**` e
`/api/auth/**` do [backend](../backend/README.md). Não precisa de SEO (tem
`<meta name="robots" content="noindex, nofollow">` no `index.html`), por isso
não usa Astro como o [site público](../site/README.md).

## Rodando localmente

```bash
cp .env.example .env   # VITE_API_BASE_URL=http://localhost:8080 por padrão
npm install
npm run dev             # http://localhost:5173
```

Login padrão de desenvolvimento: `admin@mhr.com.br` / `admin123` (criado
automaticamente pelo `SeedAdminRunner` do backend na primeira subida — **troque
essa senha redefinindo-a pelo próprio painel antes de qualquer uso real**).

## Testes

```bash
npm run test   # Vitest + Testing Library, não precisa do backend rodando
```

`apiFetch` é mockado (`vi.mock("../lib/api")`) — nenhum teste bate na API de
verdade. Cobertura prioriza `lib/` (sessão, `apiFetch`, mensagens do Zod em
português) e as telas com lógica mais sensível: `UsuariosPage` (o aviso certo
conforme `emailEnviado`), `LeadsContatoPage` (exigir cliente antes de marcar
"virou contrato"), `CandidaturasPage` (mudar status chama o `PATCH` certo) e
`ClientesPage` (busca refaz a chamada paginada, controles de página aparecem
só com mais de uma página). Telas de CRUD simples (Serviços, Depoimentos,
Blog, Vagas, Equipe, FAQ) ainda não têm teste — mesmo padrão repetido, baixo
risco.

## Autenticação (como funciona por trás das telas)

- `POST /api/auth/login` devolve um JWT + nome + papel. `src/lib/auth.ts`
  guarda os três no `localStorage` (`salvarSessao`) — é a única fonte de
  verdade de "quem está logado" no front-end.
- `src/lib/api.ts` (`apiFetch`) injeta `Authorization: Bearer <token>` em toda
  chamada à API admin automaticamente; não precisa fazer isso manualmente em
  cada tela.
- `RotaProtegida` (em `components/`) redireciona para `/login` se não há token.
  `RotaAdmin` redireciona para `/` se o usuário logado não é `ADMIN` — isso é só
  UX (esconder um link/tela de quem não devia ver), **a autorização de verdade é
  no backend** (`@PreAuthorize("hasRole('ADMIN')")`); nunca confie só no guard do
  front-end para proteger algo sensível.
- **Esqueci a senha** (`/esqueci-senha` → `/redefinir-senha?token=...`) e
  **aceitar convite** (`/aceitar-convite?token=...`) são duas telas diferentes
  por clareza de copy, mas chamam o mesmo endpoint do backend
  (`POST /api/auth/redefinir-senha`) — para o servidor, "trocar senha esquecida"
  e "ativar conta convidada" são a mesma operação. As duas pedem a senha duas
  vezes (confirmação, validada no `zod` antes de enviar) — o login não exige
  confirmação, só a própria senha. Todo campo de senha usa `CampoSenha`, com
  um botão de olho pra mostrar/ocultar o que foi digitado.

## Convidar alguém para o painel

Em **Usuários** (só visível/acessível a quem é ADMIN): preencha nome, e-mail e
papel (ADMIN ou EDITOR) e envie. O backend cria o usuário (sem senha
utilizável ainda) e manda um e-mail com o link de ativação. **Sem
`MAIL_USERNAME`/`MAIL_PASSWORD` configurados no backend**, esse e-mail não sai
de verdade — o link aparece no log do servidor (`docker logs`/console do
`bootRun`), e alguém precisa copiá-lo manualmente para a pessoa convidada.

## Estrutura

```
src/
  lib/auth.ts        # sessão (token/nome/papel) no localStorage
  lib/api.ts          # apiFetch: fetch com Authorization + erro já tratado
  lib/zod-pt-br.ts    # traduz mensagens de validação do Zod (chamado 1x em main.tsx)
  lib/mascaras.ts     # mascararTelefone/mascararCnpj ("enquanto digita")
  components/         # RotaProtegida, RotaAdmin, AdminLayout (sidebar), Modal,
                       # CampoSenha (input de senha com botão de mostrar/ocultar),
                       # CampoImagem (upload de imagem, ver POST /api/admin/uploads)
  pages/              # 1 arquivo por tela (ver tabela abaixo)
  styles.css          # paleta + classes reutilizáveis (.campo, .botao-primario,
                       # .botao-destaque, .cartao, .badge) — usadas em toda tela
                       # nova para o visual ficar consistente sem repetir Tailwind
```

| Tela | Rota | Quem acessa |
|---|---|---|
| Dashboard | `/` | logado |
| Vagas | `/vagas` | logado |
| Candidaturas | `/candidaturas` | logado |
| Serviços | `/servicos` | logado |
| Depoimentos | `/depoimentos` | logado |
| Equipe | `/equipe` | logado |
| FAQ | `/faq` | logado |
| Blog | `/blog` | logado |
| Mensagens de contato | `/leads` | logado |
| Clientes | `/clientes` | logado |
| Licitações | `/licitacoes` | logado |
| Usuários | `/usuarios` | **ADMIN** |
| Logs do sistema | `/logs` | **ADMIN** |
| Versão | `/versao` | **ADMIN** |
| Login / Esqueci senha / Redefinir / Aceitar convite | `/login`, `/esqueci-senha`, `/redefinir-senha`, `/aceitar-convite` | público |

A navegação é uma barra lateral (`components/AdminLayout.tsx`) com ícones
(lucide-react), agrupada por assunto ("Conteúdo do site", "Relacionamento",
"Licitações", "Sistema") e com grupos colapsáveis — não mais um menu
horizontal, que ficaria apertado com a quantidade de telas atual. "Sistema"
(Usuários/Logs) só aparece para quem é ADMIN. Responsiva (o sidebar vira um
menu hambúrguer/overlay em telas estreitas).

Vagas, Serviços, Depoimentos, Blog, Equipe, FAQ e Clientes têm formulário de
edição (não só criar/listar/excluir) e um campo de busca por texto simples
(filtro client-side, exceto Clientes que busca paginado no servidor — ver
abaixo), exceto Equipe/FAQ/Licitações (volume baixo, busca ainda não
compensa). Depoimentos, Blog e Clientes usam `CampoImagem` para enviar
foto/capa/logo direto do computador, além do campo de URL manual.

Clientes é a única tela paginada de verdade (servidor, 20 por página, busca
incluída) — as outras continuam buscando a lista inteira de uma vez, viável
no volume atual.

As telas (fora do login) são carregadas sob demanda (`React.lazy` +
`Suspense` em `App.tsx`) — cada uma vira seu próprio chunk JS, baixado só
quando o admin navega até ela, em vez de um bundle único grande.

## O que ainda falta (conhecido, não esquecido)

- Licitações não tem paginação de verdade — a tela principal depende da
  lista completa em memória pros contadores de cada aba de status (ver
  "Paginação" em [backend/README.md](../backend/README.md#anti-spam-observabilidade-e-auditoria)).
