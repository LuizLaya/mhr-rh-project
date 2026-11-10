import { useQuery } from "@tanstack/react-query";
import { apiFetch } from "../lib/api";

/** Espelha o SistemaInfoDto do backend (br.com.mhr.backend.sistema). */
interface SistemaInfo {
  versaoAplicacao: string;
  dataBuild: string | null;
  versaoJava: string;
  versaoSpringBoot: string;
  versaoBanco: string;
  ambiente: string;
}

function formatarData(iso: string | null) {
  if (!iso) return "—";
  return new Date(iso).toLocaleString("pt-BR", { dateStyle: "short", timeStyle: "short" });
}

interface LinhaProps {
  rotulo: string;
  valor: string;
}

function Linha({ rotulo, valor }: LinhaProps) {
  return (
    <div className="flex items-center justify-between border-b border-slate-100 py-2 text-sm last:border-0">
      <span className="text-slate-500">{rotulo}</span>
      <span className="font-mono text-mhr-navy">{valor}</span>
    </div>
  );
}

/**
 * Informações técnicas de versão/ambiente, pra tirar dúvida rápida sem precisar me
 * perguntar "qual versão está rodando" ou "o banco está atualizado". Só ADMIN acessa
 * (ver RotaAdmin em App.tsx), mesmo critério das outras telas da aba Sistema.
 */
export default function VersaoPage() {
  const { data, isLoading, error } = useQuery({
    queryKey: ["admin", "sistema", "versao"],
    queryFn: () => apiFetch<SistemaInfo>("/api/admin/sistema/versao"),
  });

  if (isLoading) return <p>Carregando...</p>;
  if (error) return <p className="text-red-600">Erro ao carregar as informações de versão.</p>;

  return (
    <div>
      <h1 className="text-2xl font-bold text-mhr-navy">Versão</h1>
      <p className="mt-1 text-sm text-slate-500">Informações técnicas do sistema em execução agora.</p>

      <div className="mt-6 grid grid-cols-1 gap-4 lg:grid-cols-2">
        <div className="cartao">
          <h2 className="mb-2 text-sm font-semibold uppercase tracking-wide text-slate-400">Painel administrativo</h2>
          <Linha rotulo="Versão" valor={__APP_VERSION__} />
        </div>

        <div className="cartao">
          <h2 className="mb-2 text-sm font-semibold uppercase tracking-wide text-slate-400">API (backend)</h2>
          <Linha rotulo="Versão" valor={data?.versaoAplicacao ?? "—"} />
          <Linha rotulo="Build gerado em" valor={formatarData(data?.dataBuild ?? null)} />
          <Linha rotulo="Java" valor={data?.versaoJava ?? "—"} />
          <Linha rotulo="Spring Boot" valor={data?.versaoSpringBoot ?? "—"} />
          <Linha rotulo="Esquema do banco (Flyway)" valor={data?.versaoBanco ?? "—"} />
          <Linha rotulo="Ambiente" valor={data?.ambiente ?? "—"} />
        </div>
      </div>
    </div>
  );
}
