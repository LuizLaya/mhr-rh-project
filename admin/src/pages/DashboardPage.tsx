import type { ReactElement, ReactNode } from "react";
import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router";
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Line,
  LineChart,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { apiFetch } from "../lib/api";
import { obterNome } from "../lib/auth";

/** Espelha o DashboardResumoDto do backend (br.com.mhr.backend.dashboard). */
interface DashboardResumo {
  vagasAbertas: number;
  candidaturasTotal: number;
  servicosPublicados: number;
  depoimentosPublicados: number;
  postsPublicados: number;
  leadsNovos: number;
  leadsTotal: number;
  usuariosAtivos: number;
  clientesTotal: number;
}

interface PontoDiario {
  data: string;
  quantidade: number;
}

/** Espelha o DashboardGraficosDto do backend. */
interface DashboardGraficos {
  leadsPorDia: PontoDiario[];
  candidaturasPorDia: PontoDiario[];
  leadsPorStatus: Record<string, number>;
  vagasPorStatus: Record<string, number>;
  conteudoPorTipo: Record<string, number>;
}

const CORES_GRAFICO = ["#102a43", "#b8893a", "#3b82f6", "#22c55e", "#ef4444"];

const ROTULO_STATUS_LEAD: Record<string, string> = {
  NOVO: "Novo",
  EM_ANDAMENTO: "Em andamento",
  CONCLUIDO: "Concluído",
};

function formatarDataCurta(iso: string) {
  const [, mes, dia] = iso.split("-");
  return `${dia}/${mes}`;
}

/** Junta os dois pontos diários (leads e candidaturas) num só array, por data, para o gráfico de linha. */
function mesclarSeriesDiarias(leads: PontoDiario[], candidaturas: PontoDiario[]) {
  const porData = new Map<string, { data: string; leads: number; candidaturas: number }>();
  for (const ponto of leads) {
    porData.set(ponto.data, { data: ponto.data, leads: ponto.quantidade, candidaturas: 0 });
  }
  for (const ponto of candidaturas) {
    const existente = porData.get(ponto.data);
    if (existente) {
      existente.candidaturas = ponto.quantidade;
    } else {
      porData.set(ponto.data, { data: ponto.data, leads: 0, candidaturas: ponto.quantidade });
    }
  }
  return [...porData.values()].sort((a, b) => a.data.localeCompare(b.data));
}

function Cartao({ titulo, valor, destaque, to }: { titulo: string; valor: number; destaque?: boolean; to: string }) {
  return (
    <Link to={to} className={`cartao transition hover:border-mhr-gold ${destaque ? "border-mhr-gold" : ""}`}>
      <p className="text-sm text-slate-500">{titulo}</p>
      <p className="mt-1 text-3xl font-bold text-mhr-navy">{valor}</p>
    </Link>
  );
}

function PainelGrafico({ titulo, children }: { titulo: string; children: ReactNode }) {
  return (
    <div className="cartao">
      <p className="mb-3 text-sm font-semibold text-mhr-navy">{titulo}</p>
      <ResponsiveContainer width="100%" height={240}>
        {children as ReactElement}
      </ResponsiveContainer>
    </div>
  );
}

/** Tela inicial do painel: visão geral do que está publicado e do que precisa de atenção. */
export default function DashboardPage() {
  const { data, isLoading, error } = useQuery({
    queryKey: ["admin", "dashboard"],
    queryFn: () => apiFetch<DashboardResumo>("/api/admin/dashboard"),
  });

  const { data: graficos } = useQuery({
    queryKey: ["admin", "dashboard", "graficos"],
    queryFn: () => apiFetch<DashboardGraficos>("/api/admin/dashboard/graficos"),
  });

  if (isLoading) return <p>Carregando...</p>;
  if (error) return <p className="text-red-600">Erro ao carregar o dashboard.</p>;
  if (!data) return null;

  const serieDiaria = graficos ? mesclarSeriesDiarias(graficos.leadsPorDia, graficos.candidaturasPorDia) : [];
  const leadsPorStatus = graficos ? Object.entries(graficos.leadsPorStatus).map(([status, valor]) => ({ nome: ROTULO_STATUS_LEAD[status] ?? status, valor })) : [];
  const vagasPorStatus = graficos ? Object.entries(graficos.vagasPorStatus).map(([status, valor]) => ({ nome: status === "ABERTA" ? "Abertas" : "Encerradas", valor })) : [];
  const conteudoPorTipo = graficos ? Object.entries(graficos.conteudoPorTipo).map(([tipo, valor]) => ({ nome: tipo, valor })) : [];

  return (
    <div>
      <h1 className="text-2xl font-bold text-mhr-navy">Olá, {obterNome() ?? "de novo"}</h1>
      <p className="mt-1 text-sm text-slate-500">Visão geral do site e do painel.</p>

      {data.leadsNovos > 0 && (
        <Link
          to="/leads"
          className="mt-4 flex flex-wrap items-center justify-between gap-2 rounded-lg bg-mhr-gold/10 border border-mhr-gold/30 px-4 py-3 text-sm text-mhr-navy transition hover:bg-mhr-gold/20"
        >
          <span>
            Você tem <strong>{data.leadsNovos}</strong> {data.leadsNovos === 1 ? "mensagem nova" : "mensagens novas"} de
            contato aguardando resposta.
          </span>
          <span className="botao-link">Ver mensagens →</span>
        </Link>
      )}

      <div className="mt-6 grid grid-cols-2 gap-4 md:grid-cols-4">
        <Cartao titulo="Vagas abertas" valor={data.vagasAbertas} to="/vagas" />
        <Cartao titulo="Candidaturas recebidas" valor={data.candidaturasTotal} to="/vagas" />
        <Cartao titulo="Serviços publicados" valor={data.servicosPublicados} to="/servicos" />
        <Cartao titulo="Depoimentos publicados" valor={data.depoimentosPublicados} to="/depoimentos" />
        <Cartao titulo="Posts do blog publicados" valor={data.postsPublicados} to="/blog" />
        <Cartao titulo="Mensagens novas" valor={data.leadsNovos} destaque={data.leadsNovos > 0} to="/leads" />
        <Cartao titulo="Mensagens no total" valor={data.leadsTotal} to="/leads" />
        <Cartao titulo="Clientes" valor={data.clientesTotal} to="/clientes" />
      </div>

      {graficos && (
        <div className="mt-8 grid grid-cols-1 gap-4 lg:grid-cols-2">
          <PainelGrafico titulo="Leads e candidaturas nos últimos 30 dias">
            <LineChart data={serieDiaria}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
              <XAxis dataKey="data" tickFormatter={formatarDataCurta} fontSize={12} />
              <YAxis allowDecimals={false} fontSize={12} />
              <Tooltip labelFormatter={(label) => formatarDataCurta(String(label))} />
              <Legend />
              <Line type="monotone" dataKey="leads" name="Mensagens" stroke={CORES_GRAFICO[0]} strokeWidth={2} dot={false} />
              <Line type="monotone" dataKey="candidaturas" name="Candidaturas" stroke={CORES_GRAFICO[1]} strokeWidth={2} dot={false} />
            </LineChart>
          </PainelGrafico>

          <PainelGrafico titulo="Conteúdo publicado por tipo">
            <BarChart data={conteudoPorTipo}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
              <XAxis dataKey="nome" fontSize={12} />
              <YAxis allowDecimals={false} fontSize={12} />
              <Tooltip />
              <Bar dataKey="valor" name="Quantidade" fill={CORES_GRAFICO[0]} radius={[4, 4, 0, 0]} />
            </BarChart>
          </PainelGrafico>

          <PainelGrafico titulo="Mensagens por status">
            <PieChart>
              <Pie data={leadsPorStatus} dataKey="valor" nameKey="nome" outerRadius={80} label>
                {leadsPorStatus.map((_, i) => <Cell key={i} fill={CORES_GRAFICO[i % CORES_GRAFICO.length]} />)}
              </Pie>
              <Tooltip />
              <Legend />
            </PieChart>
          </PainelGrafico>

          <PainelGrafico titulo="Vagas por status">
            <PieChart>
              <Pie data={vagasPorStatus} dataKey="valor" nameKey="nome" outerRadius={80} label>
                {vagasPorStatus.map((_, i) => <Cell key={i} fill={CORES_GRAFICO[(i + 2) % CORES_GRAFICO.length]} />)}
              </Pie>
              <Tooltip />
              <Legend />
            </PieChart>
          </PainelGrafico>
        </div>
      )}
    </div>
  );
}
