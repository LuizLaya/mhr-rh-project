import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiFetch } from "../lib/api";

type StatusCandidatura = "NOVA" | "EM_ANALISE" | "APROVADO" | "REJEITADO";

/** Espelha o CandidaturaDto do backend (br.com.mhr.backend.candidatura). */
interface Candidatura {
  id: string;
  vagaId: string;
  nomeCandidato: string;
  emailCandidato: string;
  telefoneCandidato?: string;
  urlCurriculo: string;
  consentimentoLgpd: boolean;
  status: StatusCandidatura;
  criadoEm: string;
}

/** Espelha o VagaDto do backend, só os campos usados aqui. */
interface Vaga {
  id: string;
  titulo: string;
}

const STATUS_LABEL: Record<StatusCandidatura, string> = {
  NOVA: "Nova",
  EM_ANALISE: "Em análise",
  APROVADO: "Aprovado",
  REJEITADO: "Rejeitado",
};

const STATUS_ESTILO: Record<StatusCandidatura, string> = {
  NOVA: "bg-blue-100 text-blue-700",
  EM_ANALISE: "bg-amber-100 text-amber-700",
  APROVADO: "bg-green-100 text-green-700",
  REJEITADO: "bg-slate-200 text-slate-600",
};

function formatarData(iso: string) {
  return new Date(iso).toLocaleString("pt-BR", { dateStyle: "short", timeStyle: "short" });
}

/**
 * Quem se candidatou a cada vaga (preenchido pelo formulário público em
 * site/src/components/react/CandidaturaForm.tsx) e o acompanhamento do processo
 * (mudar o status conforme avança a seleção).
 */
export default function CandidaturasPage() {
  const queryClient = useQueryClient();
  const [vagaFiltro, setVagaFiltro] = useState<string>("TODAS");

  const { data: vagas } = useQuery({
    queryKey: ["admin", "vagas"],
    queryFn: () => apiFetch<Vaga[]>("/api/admin/vagas"),
  });

  const { data, isLoading, error } = useQuery({
    queryKey: ["admin", "candidaturas", vagaFiltro],
    queryFn: () =>
      apiFetch<Candidatura[]>(
        vagaFiltro === "TODAS" ? "/api/admin/candidaturas" : `/api/admin/candidaturas?vagaId=${vagaFiltro}`
      ),
  });

  const atualizarStatus = useMutation({
    mutationFn: ({ id, status }: { id: string; status: StatusCandidatura }) =>
      apiFetch<Candidatura>(`/api/admin/candidaturas/${id}/status`, { method: "PATCH", body: JSON.stringify({ status }) }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["admin", "candidaturas"] }),
  });

  const tituloVaga = (vagaId: string) => vagas?.find((v) => v.id === vagaId)?.titulo ?? "—";

  if (isLoading) return <p>Carregando candidaturas...</p>;
  if (error) return <p className="text-red-600">Erro ao carregar candidaturas.</p>;

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-2xl font-bold text-mhr-navy">Candidaturas</h1>
        <select
          value={vagaFiltro}
          onChange={(e) => setVagaFiltro(e.target.value)}
          className="campo max-w-xs"
        >
          <option value="TODAS">Todas as vagas</option>
          {vagas?.map((vaga) => (
            <option key={vaga.id} value={vaga.id}>{vaga.titulo}</option>
          ))}
        </select>
      </div>

      <div className="cartao mt-4 overflow-x-auto">
        {data?.length === 0 ? (
          <p className="text-sm text-slate-500">Nenhuma candidatura recebida ainda.</p>
        ) : (
          <table className="w-full border-collapse text-left text-sm">
            <thead>
              <tr className="border-b border-slate-200">
                <th className="py-2">Candidato</th>
                <th className="py-2">Vaga</th>
                <th className="py-2">Contato</th>
                <th className="py-2">Currículo</th>
                <th className="py-2">Quando</th>
                <th className="py-2">Status</th>
              </tr>
            </thead>
            <tbody>
              {data?.map((candidatura) => (
                <tr key={candidatura.id} className="border-b border-slate-100 last:border-0">
                  <td className="py-2">{candidatura.nomeCandidato}</td>
                  <td className="min-w-0 max-w-[12rem] py-2 truncate">{tituloVaga(candidatura.vagaId)}</td>
                  <td className="py-2 text-xs text-slate-500">
                    {candidatura.emailCandidato}
                    {candidatura.telefoneCandidato ? <><br />{candidatura.telefoneCandidato}</> : null}
                  </td>
                  <td className="py-2">
                    <a href={candidatura.urlCurriculo} target="_blank" rel="noopener noreferrer" className="text-mhr-gold hover:underline">
                      Abrir
                    </a>
                  </td>
                  <td className="whitespace-nowrap py-2 text-xs text-slate-400">{formatarData(candidatura.criadoEm)}</td>
                  <td className="py-2">
                    <select
                      value={candidatura.status}
                      onChange={(e) => atualizarStatus.mutate({ id: candidatura.id, status: e.target.value as StatusCandidatura })}
                      className={`badge border-0 ${STATUS_ESTILO[candidatura.status]}`}
                    >
                      {Object.entries(STATUS_LABEL).map(([valor, rotulo]) => (
                        <option key={valor} value={valor}>{rotulo}</option>
                      ))}
                    </select>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
