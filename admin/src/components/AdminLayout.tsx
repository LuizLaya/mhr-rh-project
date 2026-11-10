import { useState } from "react";
import { NavLink, Outlet, useNavigate } from "react-router";
import { useQuery } from "@tanstack/react-query";
import {
  Briefcase,
  ChevronDown,
  ChevronUp,
  CircleHelp,
  FileText,
  Gavel,
  Info,
  LayoutDashboard,
  LogOut,
  Mail,
  Menu as MenuIcon,
  Quote,
  ScrollText,
  Settings,
  UserCheck,
  UserCog,
  Users,
  UsersRound,
  X,
  type LucideIcon,
} from "lucide-react";
import { ehAdmin, limparToken, obterNome, obterPapel } from "../lib/auth";
import { apiFetch } from "../lib/api";

interface ItemNav {
  to: string;
  label: string;
  icon: LucideIcon;
  fim?: boolean;
}

interface GrupoNav {
  titulo: string;
  itens: ItemNav[];
}

const GRUPOS_BASE: GrupoNav[] = [
  { titulo: "Visão geral", itens: [{ to: "/", label: "Dashboard", icon: LayoutDashboard, fim: true }] },
  {
    titulo: "Conteúdo do site",
    itens: [
      { to: "/vagas", label: "Vagas", icon: Briefcase },
      { to: "/candidaturas", label: "Candidaturas", icon: UserCheck },
      { to: "/servicos", label: "Serviços", icon: Settings },
      { to: "/depoimentos", label: "Depoimentos", icon: Quote },
      { to: "/equipe", label: "Equipe", icon: UsersRound },
      { to: "/faq", label: "FAQ", icon: CircleHelp },
      { to: "/blog", label: "Blog", icon: FileText },
    ],
  },
  {
    titulo: "Relacionamento",
    itens: [
      { to: "/leads", label: "Mensagens", icon: Mail },
      { to: "/clientes", label: "Clientes", icon: Users },
    ],
  },
  {
    titulo: "Licitações",
    itens: [{ to: "/licitacoes", label: "Licitações", icon: Gavel }],
  },
];

const GRUPO_SISTEMA: GrupoNav = {
  titulo: "Sistema",
  itens: [
    { to: "/usuarios", label: "Usuários", icon: UserCog },
    { to: "/logs", label: "Logs", icon: ScrollText },
    { to: "/versao", label: "Versão", icon: Info },
  ],
};

/**
 * Casca do painel admin: barra lateral fixa com a navegação agrupada (em vez do menu
 * horizontal antigo, que ficaria apertado com a quantidade de telas que o painel já
 * tem) + o conteúdo da rota atual (via `Outlet` do react-router). "Sistema" só
 * aparece para ADMIN, mesmo critério que já existia para a aba Usuários. Cada grupo
 * pode ser recolhido (estado local, não persiste entre sessões de propósito — é só
 * uma conveniência de tela, não uma preferência que precise sobreviver a um reload).
 */
export default function AdminLayout() {
  const navigate = useNavigate();
  const [menuAberto, setMenuAberto] = useState(false);
  const [gruposRecolhidos, setGruposRecolhidos] = useState<Set<string>>(new Set());
  const grupos = ehAdmin() ? [...GRUPOS_BASE, GRUPO_SISTEMA] : GRUPOS_BASE;

  // Polling simples (sem WebSocket) para o badge de mensagens novas ficar
  // razoavelmente atualizado enquanto o admin usa o painel.
  const { data: resumo } = useQuery({
    queryKey: ["admin", "dashboard"],
    queryFn: () => apiFetch<{ leadsNovos: number }>("/api/admin/dashboard"),
    refetchInterval: 60_000,
  });
  const mensagensNovas = resumo?.leadsNovos ?? 0;

  function sair() {
    limparToken();
    navigate("/login");
  }

  function alternarGrupo(titulo: string) {
    setGruposRecolhidos((atual) => {
      const novo = new Set(atual);
      if (novo.has(titulo)) novo.delete(titulo);
      else novo.add(titulo);
      return novo;
    });
  }

  const conteudoNav = (
    <>
      <div className="flex items-center justify-between">
        <img src="/logo.png" alt="MHR" className="h-9 w-auto" />
        <button onClick={() => setMenuAberto(false)} aria-label="Fechar menu" className="text-slate-400 md:hidden">
          <X size={22} />
        </button>
      </div>

      <nav className="mt-8 flex-1 space-y-4 overflow-y-auto">
        {grupos.map((grupo) => {
          const recolhido = gruposRecolhidos.has(grupo.titulo);
          return (
            <div key={grupo.titulo}>
              <button
                onClick={() => alternarGrupo(grupo.titulo)}
                className="flex w-full items-center justify-between px-3 py-1 text-xs font-semibold uppercase tracking-wider text-slate-400 hover:text-slate-600"
              >
                {grupo.titulo}
                {recolhido ? <ChevronDown size={14} /> : <ChevronUp size={14} />}
              </button>
              {!recolhido && (
                <div className="mt-2 space-y-1">
                  {grupo.itens.map((item) => (
                    <NavLink
                      key={item.to}
                      to={item.to}
                      end={item.fim}
                      onClick={() => setMenuAberto(false)}
                      className={({ isActive }) =>
                        `flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition ${
                          isActive ? "bg-mhr-navy text-white" : "text-slate-600 hover:bg-slate-100"
                        }`
                      }
                    >
                      <item.icon size={18} className="shrink-0" />
                      <span className="flex-1">{item.label}</span>
                      {item.to === "/leads" && mensagensNovas > 0 && (
                        <span className="badge bg-mhr-gold text-white">{mensagensNovas}</span>
                      )}
                    </NavLink>
                  ))}
                </div>
              )}
            </div>
          );
        })}
      </nav>

      <div className="border-t border-slate-200 pt-4">
        <p className="px-3 text-sm font-medium text-mhr-navy">{obterNome() ?? "Usuário"}</p>
        <p className="px-3 text-xs text-slate-400">{obterPapel() === "ADMIN" ? "Admin" : "Editor"}</p>
        <button onClick={sair} className="botao-link mt-2 flex w-full items-center gap-2 px-3 text-left">
          <LogOut size={16} /> Sair
        </button>
      </div>
    </>
  );

  return (
    <div className="min-h-screen bg-slate-50 md:flex">
      {/* Topo mobile: logo + botao de menu, a sidebar vira um overlay (ver aside abaixo) */}
      <header className="flex items-center justify-between border-b border-slate-200 bg-white px-4 py-3 md:hidden">
        <img src="/logo.png" alt="MHR" className="h-8 w-auto" />
        <button
          onClick={() => setMenuAberto((v) => !v)}
          aria-label="Abrir menu"
          className="rounded-lg border border-slate-200 p-2 text-mhr-navy"
        >
          <MenuIcon size={20} />
        </button>
      </header>

      {menuAberto && (
        <div className="fixed inset-0 z-30 bg-black/30 md:hidden" onClick={() => setMenuAberto(false)} />
      )}

      <aside
        className={`fixed inset-y-0 left-0 z-40 flex w-64 flex-col border-r border-slate-200 bg-white p-4 transition-transform md:static md:z-auto md:translate-x-0 ${
          menuAberto ? "translate-x-0" : "-translate-x-full"
        }`}
      >
        {conteudoNav}
      </aside>

      <main className="min-w-0 flex-1 p-4 sm:p-6">
        <div className="mx-auto max-w-5xl">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
