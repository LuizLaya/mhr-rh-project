import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Eye, Trash2, TriangleAlert } from "lucide-react";
import { apiFetch } from "../lib/api";
import Modal from "../components/Modal";

type Nivel = "INFO" | "AVISO" | "ERRO";

/** Espelha o LogSistemaDto do backend (br.com.mhr.backend.logsistema). */
interface LogSistema {
  id: string;
  nivel: Nivel;
  categoria: string;
  mensagem: string;
  usuarioEmail?: string;
  origem?: string;
  endpoint?: string;
  correlacaoId?: string;
  criadoEm: string;
}

const ESTILO_NIVEL: Record<Nivel, string> = {
  INFO: "bg-blue-100 text-blue-700",
  AVISO: "bg-amber-100 text-amber-700",
  ERRO: "bg-red-100 text-red-700",
};

const NIVEIS: Nivel[] = ["INFO", "AVISO", "ERRO"];

function formatarData(iso: string) {
  return new Date(iso).toLocaleString("pt-BR", { dateStyle: "short", timeStyle: "medium" });
}

/**
 * Últimos eventos do sistema (login, envio de e-mail, erros inesperados) - só ADMIN
 * acessa (ver RotaAdmin em App.tsx). A tabela é limpa automaticamente toda semana no
 * backend (LogSistemaService.limparAntigos), então esta lista mostra no máximo os
 * últimos 7 dias; o admin também pode apagar manualmente (um log ou tudo) para tirar
 * ruído antes disso, como uma sequência de erros já resolvidos.
 */
export default function LogsPage() {
  const queryClient = useQueryClient();
  const [filtro, setFiltro] = useState<Nivel | "TODOS">("TODOS");
  const [logSelecionado, setLogSelecionado] = useState<LogSistema | null>(null);
  const [confirmandoLimpeza, setConfirmandoLimpeza] = useState(false);

  const { data, isLoading, error } = useQuery({
    queryKey: ["admin", "logs"],
    queryFn: () => apiFetch<LogSistema[]>("/api/admin/logs"),
    refetchInterval: 30_000,
  });

  const invalidar = () => queryClient.invalidateQueries({ queryKey: ["admin", "logs"] });

  const excluir = useMutation({
    mutationFn: (id: string) => apiFetch<void>(`/api/admin/logs/${id}`, { method: "DELETE" }),
    onSuccess: invalidar,
  });

  const limparTudo = useMutation({
    mutationFn: () => apiFetch<void>("/api/admin/logs", { method: "DELETE" }),
    onSuccess: () => {
      invalidar();
      setConfirmandoLimpeza(false);
    },
  });

  if (isLoading) return <p>Carregando logs...</p>;
  if (error) return <p className="text-red-600">Erro ao carregar os logs.</p>;

  const logs = (data ?? []).filter((log) => filtro === "TODOS" || log.nivel === filtro);

  return (
    <div>
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-mhr-navy">Logs do sistema</h1>
          <p className="mt-1 text-sm text-slate-500">
            Login, envio de e-mail e erros inesperados dos últimos 7 dias (a tabela é limpa automaticamente toda semana).
          </p>
        </div>
        {data && data.length > 0 && (
          <button onClick={() => setConfirmandoLimpeza(true)} className="text-sm text-red-600 hover:underline">
            Limpar tudo
          </button>
        )}
      </div>

      <div className="mt-4 flex flex-wrap gap-2">
        <button
          onClick={() => setFiltro("TODOS")}
          className={`badge border ${filtro === "TODOS" ? "border-mhr-navy bg-mhr-navy text-white" : "border-slate-200 text-slate-600"}`}
        >
          Todos ({data?.length ?? 0})
        </button>
        {NIVEIS.map((nivel) => (
          <button
            key={nivel}
            onClick={() => setFiltro(nivel)}
            className={`badge border ${filtro === nivel ? "border-mhr-navy bg-mhr-navy text-white" : `border-transparent ${ESTILO_NIVEL[nivel]}`}`}
          >
            {nivel} ({data?.filter((log) => log.nivel === nivel).length ?? 0})
          </button>
        ))}
      </div>

      <div className="cartao mt-4 overflow-x-auto">
        {logs.length === 0 ? (
          <p className="text-sm text-slate-500">Nenhum evento registrado com esse filtro.</p>
        ) : (
          <table className="w-full border-collapse text-left text-sm">
            <thead>
              <tr className="border-b border-slate-200">
                <th className="py-2">Quando</th>
                <th className="py-2">Nível</th>
                <th className="py-2">Categoria</th>
                <th className="py-2">Mensagem</th>
                <th className="py-2">E-mail relacionado</th>
                <th className="py-2" />
              </tr>
            </thead>
            <tbody>
              {logs.map((log) => (
                <tr key={log.id} className="border-b border-slate-100 last:border-0">
                  <td className="whitespace-nowrap py-2 text-xs text-slate-400">{formatarData(log.criadoEm)}</td>
                  <td className="py-2"><span className={`badge ${ESTILO_NIVEL[log.nivel]}`}>{log.nivel}</span></td>
                  <td className="py-2 text-xs text-slate-500">{log.categoria}</td>
                  <td className="max-w-xs py-2">
                    <div className="flex items-center gap-2">
                      <span className="truncate">{log.mensagem}</span>
                      <button
                        onClick={() => setLogSelecionado(log)}
                        aria-label="Ver mensagem completa"
                        className="shrink-0 text-slate-400 hover:text-mhr-navy"
                      >
                        <Eye size={16} />
                      </button>
                    </div>
                  </td>
                  <td className="py-2 text-xs text-slate-500">{log.usuarioEmail ?? "—"}</td>
                  <td className="py-2 text-right">
                    <button
                      onClick={() => excluir.mutate(log.id)}
                      aria-label="Excluir log"
                      className="text-slate-400 hover:text-red-600"
                    >
                      <Trash2 size={16} />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <Modal titulo={logSelecionado ? `${logSelecionado.categoria} · ${logSelecionado.nivel}` : ""} aberto={logSelecionado != null} aoFechar={() => setLogSelecionado(null)}>
        {logSelecionado && (
          <>
            <p className="text-xs text-slate-400">
              {formatarData(logSelecionado.criadoEm)}
              {logSelecionado.usuarioEmail ? ` · ${logSelecionado.usuarioEmail}` : ""}
            </p>
            <p className="mt-2 whitespace-pre-wrap break-words">{logSelecionado.mensagem}</p>
            {(logSelecionado.origem || logSelecionado.endpoint || logSelecionado.correlacaoId) && (
              <dl className="mt-3 space-y-1 border-t border-slate-100 pt-3 text-xs text-slate-500">
                {logSelecionado.origem && (
                  <div className="flex gap-2">
                    <dt className="font-medium text-slate-400">Origem:</dt>
                    <dd className="font-mono">{logSelecionado.origem}</dd>
                  </div>
                )}
                {logSelecionado.endpoint && (
                  <div className="flex gap-2">
                    <dt className="font-medium text-slate-400">Endpoint:</dt>
                    <dd className="font-mono">{logSelecionado.endpoint}</dd>
                  </div>
                )}
                {logSelecionado.correlacaoId && (
                  <div className="flex gap-2">
                    <dt className="font-medium text-slate-400">Execução:</dt>
                    <dd className="font-mono" title="Mesmo id = mesma rodada da rotina agendada">
                      {logSelecionado.correlacaoId}
                    </dd>
                  </div>
                )}
              </dl>
            )}
          </>
        )}
      </Modal>

      <Modal titulo="Apagar todos os logs?" aberto={confirmandoLimpeza} aoFechar={() => setConfirmandoLimpeza(false)}>
        <div className="flex items-start gap-3">
          <TriangleAlert className="mt-0.5 shrink-0 text-red-500" size={20} />
          <p>
            Essa ação remove os {data?.length ?? 0} registros atuais e <strong>não pode ser desfeita</strong>.
          </p>
        </div>
        <div className="mt-5 flex justify-end gap-2">
          <button
            onClick={() => setConfirmandoLimpeza(false)}
            className="rounded-md px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-100"
          >
            Cancelar
          </button>
          <button
            onClick={() => limparTudo.mutate()}
            disabled={limparTudo.isPending}
            className="rounded-md bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {limparTudo.isPending ? "Apagando..." : "Apagar tudo"}
          </button>
        </div>
      </Modal>
    </div>
  );
}
