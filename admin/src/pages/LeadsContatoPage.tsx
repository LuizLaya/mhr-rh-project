import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiFetch } from "../lib/api";
import type { Cliente } from "./ClientesPage";

type StatusLead = "NOVO" | "EM_ANDAMENTO" | "CONCLUIDO";

/** Espelha o LeadContatoDto do backend (br.com.mhr.backend.leadcontato). */
interface LeadContato {
  id: string;
  nome: string;
  email: string;
  telefone?: string;
  nomeEmpresa?: string;
  mensagem?: string;
  status: StatusLead;
  concluidoEm: string | null;
  virouContrato: boolean;
  clienteId: string | null;
  criadoEm: string;
}

const OPCOES_STATUS: StatusLead[] = ["NOVO", "EM_ANDAMENTO", "CONCLUIDO"];

const ESTILO_BADGE: Record<StatusLead, string> = {
  NOVO: "bg-blue-100 text-blue-700",
  EM_ANDAMENTO: "bg-amber-100 text-amber-700",
  CONCLUIDO: "bg-green-100 text-green-700",
};

const ROTULO_STATUS: Record<StatusLead, string> = {
  NOVO: "Novo",
  EM_ANDAMENTO: "Em andamento",
  CONCLUIDO: "Concluído",
};

function formatarData(iso: string) {
  return new Date(iso).toLocaleString("pt-BR", { dateStyle: "short", timeStyle: "short" });
}

function ControleContrato({
  lead,
  clientes,
  onSalvar,
}: {
  lead: LeadContato;
  clientes: Cliente[] | undefined;
  onSalvar: (args: { id: string; virouContrato: boolean; clienteId: string | null }) => void;
}) {
  const [escolhendo, setEscolhendo] = useState(false);
  const [clienteSelecionado, setClienteSelecionado] = useState(lead.clienteId ?? "");

  if (lead.virouContrato) {
    const cliente = clientes?.find((c) => c.id === lead.clienteId);
    return (
      <div className="flex items-center gap-2 text-xs">
        <span className="badge bg-mhr-gold/20 text-mhr-gold">Virou contrato · {cliente?.nomeEmpresa ?? "cliente"}</span>
        <button
          onClick={() => onSalvar({ id: lead.id, virouContrato: false, clienteId: null })}
          className="text-slate-400 hover:underline"
        >
          Desfazer
        </button>
      </div>
    );
  }

  if (!escolhendo) {
    return (
      <button onClick={() => setEscolhendo(true)} className="botao-link text-xs">
        Marcar como contrato
      </button>
    );
  }

  return (
    <div className="flex items-center gap-2">
      <select
        value={clienteSelecionado}
        onChange={(e) => setClienteSelecionado(e.target.value)}
        className="campo w-auto py-1 text-xs"
      >
        <option value="">Selecione o cliente…</option>
        {clientes?.map((cliente) => (
          <option key={cliente.id} value={cliente.id}>{cliente.nomeEmpresa}</option>
        ))}
      </select>
      <button
        disabled={!clienteSelecionado}
        onClick={() => {
          onSalvar({ id: lead.id, virouContrato: true, clienteId: clienteSelecionado });
          setEscolhendo(false);
        }}
        className="botao-link text-xs disabled:opacity-40"
      >
        Confirmar
      </button>
      <button onClick={() => setEscolhendo(false)} className="text-xs text-slate-400 hover:underline">
        Cancelar
      </button>
    </div>
  );
}

// Sem formulario de criacao aqui: leads vem do formulario de contato do site publico
// (POST /api/leads-contato). O admin so le, atualiza o status de acompanhamento e
// marca quando um lead vira cliente de verdade.
export default function LeadsContatoPage() {
  const queryClient = useQueryClient();

  const { data, isLoading, error } = useQuery({
    queryKey: ["admin", "leads-contato"],
    queryFn: () => apiFetch<LeadContato[]>("/api/admin/leads-contato"),
  });

  const { data: clientes } = useQuery({
    queryKey: ["admin", "clientes"],
    queryFn: () => apiFetch<Cliente[]>("/api/admin/clientes"),
  });

  const atualizarStatus = useMutation({
    mutationFn: ({ id, status }: { id: string; status: StatusLead }) =>
      apiFetch<LeadContato>(`/api/admin/leads-contato/${id}/status`, {
        method: "PATCH",
        body: JSON.stringify({ status }),
      }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["admin", "leads-contato"] }),
  });

  const atualizarCliente = useMutation({
    mutationFn: ({ id, virouContrato, clienteId }: { id: string; virouContrato: boolean; clienteId: string | null }) =>
      apiFetch<LeadContato>(`/api/admin/leads-contato/${id}/cliente`, {
        method: "PATCH",
        body: JSON.stringify({ virouContrato, clienteId }),
      }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["admin", "leads-contato"] }),
  });

  if (isLoading) return <p>Carregando mensagens...</p>;
  if (error) return <p className="text-red-600">Erro ao carregar mensagens.</p>;

  const leads = [...(data ?? [])].sort((a, b) => b.criadoEm.localeCompare(a.criadoEm));

  return (
    <div>
      <h1 className="text-2xl font-bold text-mhr-navy">Mensagens de contato</h1>

      {leads.length === 0 && <p className="mt-4 text-slate-600">Nenhuma mensagem recebida ainda.</p>}

      <div className="mt-6 flex flex-col gap-4">
        {leads.map((lead) => (
          <div key={lead.id} className="cartao">
            <div className="flex flex-wrap items-start justify-between gap-4">
              <div className="min-w-0 flex-1 break-words">
                <p className="font-semibold text-mhr-navy">
                  {lead.nome} {lead.nomeEmpresa ? `· ${lead.nomeEmpresa}` : ""}
                </p>
                <p className="text-sm text-slate-500">
                  {lead.email}{lead.telefone ? ` · ${lead.telefone}` : ""}
                </p>
                <p className="mt-1 text-xs text-slate-400">Recebida em {formatarData(lead.criadoEm)}</p>
              </div>
              <div className="flex flex-col items-end gap-2">
                <span className={`badge ${ESTILO_BADGE[lead.status]}`}>{ROTULO_STATUS[lead.status]}</span>
                <select
                  value={lead.status}
                  onChange={(e) => atualizarStatus.mutate({ id: lead.id, status: e.target.value as StatusLead })}
                  className="campo w-auto py-1 text-xs"
                >
                  {OPCOES_STATUS.map((status) => (
                    <option key={status} value={status}>{ROTULO_STATUS[status]}</option>
                  ))}
                </select>
              </div>
            </div>

            {lead.mensagem && <p className="mt-3 text-sm text-slate-700">{lead.mensagem}</p>}

            <div className="mt-3 flex items-center justify-between gap-4 border-t border-slate-100 pt-3">
              {lead.status === "CONCLUIDO" && lead.concluidoEm ? (
                <p className="text-xs text-green-700">Concluída em {formatarData(lead.concluidoEm)}</p>
              ) : <span />}
              <ControleContrato lead={lead} clientes={clientes} onSalvar={(args) => atualizarCliente.mutate(args)} />
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
