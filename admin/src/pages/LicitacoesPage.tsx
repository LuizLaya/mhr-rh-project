import { useEffect, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { apiFetch } from "../lib/api";

type Origem = "PNCP_AUTOMATICO" | "MANUAL";
type Status = "IDENTIFICADA" | "EM_ANALISE" | "DOCUMENTACAO" | "PROPOSTA_ENVIADA" | "GANHOU" | "PERDEU" | "DESCARTADA";

/** Espelha o LicitacaoDto do backend (br.com.mhr.backend.licitacao). */
interface Licitacao {
  id: string;
  numeroControlePncp?: string;
  origem: Origem;
  orgao: string;
  objeto: string;
  modalidade?: string;
  numeroProcesso?: string;
  uf?: string;
  valorEstimado?: number;
  dataAberturaProposta?: string;
  dataEncerramentoProposta?: string;
  linkEdital?: string;
  responsavelId?: string;
  status: Status;
  observacoes?: string;
  criadoEm: string;
}

interface DocumentoLicitacao {
  id: string;
  licitacaoId: string;
  nome: string;
  concluido: boolean;
}

interface Responsavel {
  id: string;
  nome: string;
}

interface Configuracao {
  buscaPncpAtiva: boolean;
  palavrasChave: string;
}

interface ResultadoSincronizacao {
  analisadas: number;
  importadas: number;
}

const OPCOES_STATUS: Status[] = [
  "IDENTIFICADA", "EM_ANALISE", "DOCUMENTACAO", "PROPOSTA_ENVIADA", "GANHOU", "PERDEU", "DESCARTADA",
];

const ROTULO_STATUS: Record<Status, string> = {
  IDENTIFICADA: "Identificada",
  EM_ANALISE: "Em análise",
  DOCUMENTACAO: "Documentação",
  PROPOSTA_ENVIADA: "Proposta enviada",
  GANHOU: "Ganhou",
  PERDEU: "Perdeu",
  DESCARTADA: "Descartada",
};

const ESTILO_BADGE: Record<Status, string> = {
  IDENTIFICADA: "bg-blue-100 text-blue-700",
  EM_ANALISE: "bg-amber-100 text-amber-700",
  DOCUMENTACAO: "bg-amber-100 text-amber-700",
  PROPOSTA_ENVIADA: "bg-purple-100 text-purple-700",
  GANHOU: "bg-green-100 text-green-700",
  PERDEU: "bg-red-100 text-red-700",
  DESCARTADA: "bg-slate-100 text-slate-600",
};

const licitacaoSchema = z.object({
  orgao: z.string().min(2),
  objeto: z.string().min(5),
  modalidade: z.string().optional(),
  numeroProcesso: z.string().optional(),
  uf: z.string().optional(),
  valorEstimado: z.coerce.number().optional(),
  dataAberturaProposta: z.string().optional(),
  dataEncerramentoProposta: z.string().optional(),
  linkEdital: z.string().optional(),
  observacoes: z.string().optional(),
});

type LicitacaoFormValues = z.infer<typeof licitacaoSchema>;

function formatarData(iso?: string) {
  if (!iso) return "—";
  return new Date(iso).toLocaleDateString("pt-BR", { day: "2-digit", month: "2-digit", year: "numeric" });
}

function formatarValor(valor?: number) {
  if (valor == null) return "—";
  return valor.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

// "Prazo perto de vencer": mesma janela de 2 dias usada no alerta por e-mail do backend.
function prazoProximo(iso?: string) {
  if (!iso) return false;
  const diffMs = new Date(iso).getTime() - Date.now();
  return diffMs > 0 && diffMs < 2 * 24 * 60 * 60 * 1000;
}

function paraIsoOuNulo(valorDatetimeLocal?: string) {
  return valorDatetimeLocal ? new Date(valorDatetimeLocal).toISOString() : null;
}

function PainelDocumentos({ licitacaoId }: { licitacaoId: string }) {
  const queryClient = useQueryClient();
  const [novoDocumento, setNovoDocumento] = useState("");

  const { data } = useQuery({
    queryKey: ["admin", "licitacoes", licitacaoId, "documentos"],
    queryFn: () => apiFetch<DocumentoLicitacao[]>(`/api/admin/licitacoes/${licitacaoId}/documentos`),
  });

  const invalidar = () =>
    queryClient.invalidateQueries({ queryKey: ["admin", "licitacoes", licitacaoId, "documentos"] });

  const criar = useMutation({
    mutationFn: (nome: string) =>
      apiFetch(`/api/admin/licitacoes/${licitacaoId}/documentos`, { method: "POST", body: JSON.stringify({ nome }) }),
    onSuccess: () => {
      invalidar();
      setNovoDocumento("");
    },
  });
  const atualizar = useMutation({
    mutationFn: ({ id, concluido }: { id: string; concluido: boolean }) =>
      apiFetch(`/api/admin/licitacoes/documentos/${id}`, { method: "PATCH", body: JSON.stringify({ concluido }) }),
    onSuccess: invalidar,
  });
  const excluir = useMutation({
    mutationFn: (id: string) => apiFetch(`/api/admin/licitacoes/documentos/${id}`, { method: "DELETE" }),
    onSuccess: invalidar,
  });

  return (
    <div>
      <p className="text-xs font-semibold uppercase tracking-wider text-slate-400">Documentos exigidos</p>
      <ul className="mt-2 space-y-1">
        {data?.map((documento) => (
          <li key={documento.id} className="flex items-center gap-2 text-sm">
            <input
              type="checkbox"
              checked={documento.concluido}
              onChange={(e) => atualizar.mutate({ id: documento.id, concluido: e.target.checked })}
            />
            <span className={documento.concluido ? "text-slate-400 line-through" : ""}>{documento.nome}</span>
            <button onClick={() => excluir.mutate(documento.id)} className="ml-auto text-xs text-red-600 hover:underline">
              Remover
            </button>
          </li>
        ))}
        {data?.length === 0 && <li className="text-sm text-slate-400">Nenhum documento cadastrado.</li>}
      </ul>
      <form
        onSubmit={(e) => {
          e.preventDefault();
          if (novoDocumento.trim()) criar.mutate(novoDocumento.trim());
        }}
        className="mt-2 flex gap-2"
      >
        <input
          value={novoDocumento}
          onChange={(e) => setNovoDocumento(e.target.value)}
          placeholder="Novo documento..."
          className="campo text-sm"
        />
        <button type="submit" className="botao-link text-sm">Adicionar</button>
      </form>
    </div>
  );
}

function CartaoLicitacao({ licitacao, responsaveis }: { licitacao: Licitacao; responsaveis: Responsavel[] | undefined }) {
  const [expandido, setExpandido] = useState(false);
  const queryClient = useQueryClient();

  const invalidar = () => queryClient.invalidateQueries({ queryKey: ["admin", "licitacoes"] });

  const atualizarStatus = useMutation({
    mutationFn: (status: Status) =>
      apiFetch(`/api/admin/licitacoes/${licitacao.id}/status`, { method: "PATCH", body: JSON.stringify({ status }) }),
    onSuccess: invalidar,
  });
  const atualizarResponsavel = useMutation({
    mutationFn: (responsavelId: string) =>
      apiFetch(`/api/admin/licitacoes/${licitacao.id}/responsavel`, {
        method: "PATCH",
        body: JSON.stringify({ responsavelId: responsavelId || null }),
      }),
    onSuccess: invalidar,
  });
  const excluir = useMutation({
    mutationFn: () => apiFetch(`/api/admin/licitacoes/${licitacao.id}`, { method: "DELETE" }),
    onSuccess: invalidar,
  });

  const prazoUrgente = prazoProximo(licitacao.dataEncerramentoProposta);

  return (
    <div className="cartao">
      <button onClick={() => setExpandido((v) => !v)} className="flex w-full flex-wrap items-start justify-between gap-4 text-left">
        <div className="min-w-0 flex-1">
          <div className="flex flex-wrap items-center gap-2">
            <span className={`badge ${licitacao.origem === "PNCP_AUTOMATICO" ? "bg-mhr-navy text-white" : "bg-slate-100 text-slate-600"}`}>
              {licitacao.origem === "PNCP_AUTOMATICO" ? "PNCP" : "Manual"}
            </span>
            <p className="font-semibold text-mhr-navy">{licitacao.orgao}</p>
          </div>
          <p className="mt-1 line-clamp-2 text-sm text-slate-600">{licitacao.objeto}</p>
          <p className="mt-1 text-xs text-slate-400">
            {licitacao.modalidade ?? "Modalidade não informada"} · {formatarValor(licitacao.valorEstimado)}
            {licitacao.dataEncerramentoProposta && (
              <span className={prazoUrgente ? "ml-1 font-semibold text-red-600" : "ml-1"}>
                · Prazo: {formatarData(licitacao.dataEncerramentoProposta)}
              </span>
            )}
          </p>
        </div>
        <span className={`badge shrink-0 ${ESTILO_BADGE[licitacao.status]}`}>{ROTULO_STATUS[licitacao.status]}</span>
      </button>

      {expandido && (
        <div className="mt-4 space-y-3 border-t border-slate-100 pt-3">
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            <label className="text-sm">
              Status
              <select
                value={licitacao.status}
                onChange={(e) => atualizarStatus.mutate(e.target.value as Status)}
                className="campo mt-1"
              >
                {OPCOES_STATUS.map((status) => (
                  <option key={status} value={status}>{ROTULO_STATUS[status]}</option>
                ))}
              </select>
            </label>
            <label className="text-sm">
              Responsável
              <select
                value={licitacao.responsavelId ?? ""}
                onChange={(e) => atualizarResponsavel.mutate(e.target.value)}
                className="campo mt-1"
              >
                <option value="">Sem responsável</option>
                {responsaveis?.map((responsavel) => (
                  <option key={responsavel.id} value={responsavel.id}>{responsavel.nome}</option>
                ))}
              </select>
            </label>
          </div>
          {licitacao.linkEdital && (
            <a href={licitacao.linkEdital} target="_blank" rel="noreferrer" className="botao-link inline-block text-sm">
              Abrir edital →
            </a>
          )}
          {licitacao.numeroProcesso && <p className="text-xs text-slate-500">Processo: {licitacao.numeroProcesso}</p>}
          {licitacao.observacoes && <p className="text-sm text-slate-700">{licitacao.observacoes}</p>}
          <PainelDocumentos licitacaoId={licitacao.id} />
          <button onClick={() => excluir.mutate()} className="text-xs text-red-600 hover:underline">
            Excluir licitação
          </button>
        </div>
      )}
    </div>
  );
}

/**
 * Acompanhamento de licitações públicas: licitações entram aqui tanto pela
 * busca automática no PNCP (ver botão/toggle no topo) quanto por cadastro
 * manual, e seguem um funil de status até o resultado.
 */
export default function LicitacoesPage() {
  const queryClient = useQueryClient();
  const [filtro, setFiltro] = useState<Status | "TODAS">("TODAS");
  const [mostrarForm, setMostrarForm] = useState(false);

  const { data, isLoading, error } = useQuery({
    queryKey: ["admin", "licitacoes"],
    queryFn: () => apiFetch<Licitacao[]>("/api/admin/licitacoes"),
  });

  const { data: responsaveis } = useQuery({
    queryKey: ["admin", "licitacoes", "responsaveis"],
    queryFn: () => apiFetch<Responsavel[]>("/api/admin/licitacoes/responsaveis"),
  });

  const { data: configuracao } = useQuery({
    queryKey: ["admin", "licitacoes", "configuracao"],
    queryFn: () => apiFetch<Configuracao>("/api/admin/licitacoes/configuracao"),
  });

  const [palavrasChave, setPalavrasChave] = useState("");
  useEffect(() => {
    if (configuracao) setPalavrasChave(configuracao.palavrasChave);
  }, [configuracao?.palavrasChave]);

  const salvarConfiguracao = useMutation({
    mutationFn: (dados: { buscaPncpAtiva: boolean; palavrasChave: string }) =>
      apiFetch<Configuracao>("/api/admin/licitacoes/configuracao", {
        method: "PATCH",
        body: JSON.stringify(dados),
      }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["admin", "licitacoes", "configuracao"] }),
  });

  const sincronizar = useMutation({
    mutationFn: () => apiFetch<ResultadoSincronizacao>("/api/admin/licitacoes/sincronizar", { method: "POST" }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["admin", "licitacoes"] }),
  });

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<LicitacaoFormValues>({ resolver: zodResolver(licitacaoSchema) });

  const criar = useMutation({
    mutationFn: (values: LicitacaoFormValues) =>
      apiFetch<Licitacao>("/api/admin/licitacoes", {
        method: "POST",
        body: JSON.stringify({
          ...values,
          dataAberturaProposta: paraIsoOuNulo(values.dataAberturaProposta),
          dataEncerramentoProposta: paraIsoOuNulo(values.dataEncerramentoProposta),
        }),
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "licitacoes"] });
      reset();
      setMostrarForm(false);
    },
  });

  if (isLoading) return <p>Carregando licitações...</p>;
  if (error) return <p className="text-red-600">Erro ao carregar licitações.</p>;

  const todas = data ?? [];
  const listaFiltrada = filtro === "TODAS" ? todas : todas.filter((l) => l.status === filtro);

  return (
    <div>
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-mhr-navy">Licitações</h1>
        <button onClick={() => setMostrarForm((v) => !v)} className="botao-primario">
          {mostrarForm ? "Cancelar" : "Nova licitação"}
        </button>
      </div>

      <div className="cartao mt-4 flex flex-col gap-4">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div className="flex items-center gap-2">
            <label className="flex items-center gap-2 text-sm font-medium text-mhr-navy">
              <input
                type="checkbox"
                checked={configuracao?.buscaPncpAtiva ?? false}
                onChange={(e) =>
                  salvarConfiguracao.mutate({ buscaPncpAtiva: e.target.checked, palavrasChave: configuracao?.palavrasChave ?? "" })
                }
              />
              Busca automática no PNCP
            </label>
            <span className="text-xs text-slate-400">
              {configuracao?.buscaPncpAtiva ? "Rodando todo dia às 7h" : "Desligada — ligue para buscar"}
            </span>
          </div>
          <button
            onClick={() => sincronizar.mutate()}
            disabled={!configuracao?.buscaPncpAtiva || sincronizar.isPending}
            className="botao-link text-sm disabled:cursor-not-allowed disabled:opacity-40"
          >
            {sincronizar.isPending ? "Buscando..." : "Buscar agora"}
          </button>
        </div>

        <div>
          <label className="text-sm font-medium text-mhr-navy">Palavras-chave da busca</label>
          <p className="mt-1 text-xs text-slate-400">
            Separadas por vírgula. O PNCP não tem busca por palavra-chave própria — o sistema compara cada termo
            com o objeto de cada contratação, então ajustar essa lista muda o que é encontrado automaticamente.
          </p>
          <textarea
            value={palavrasChave}
            onChange={(e) => setPalavrasChave(e.target.value)}
            rows={2}
            className="campo mt-2"
            placeholder="ex.: recrutamento, treinamento, consultoria em rh"
          />
          <button
            onClick={() => salvarConfiguracao.mutate({ buscaPncpAtiva: configuracao?.buscaPncpAtiva ?? false, palavrasChave })}
            disabled={salvarConfiguracao.isPending || !palavrasChave.trim()}
            className="botao-link mt-2 text-sm disabled:cursor-not-allowed disabled:opacity-40"
          >
            {salvarConfiguracao.isPending ? "Salvando..." : "Salvar palavras-chave"}
          </button>
        </div>
      </div>
      {sincronizar.isSuccess && (
        <p className="mt-2 text-sm text-green-700">
          {sincronizar.data.analisadas} contratações analisadas, {sincronizar.data.importadas} licitações novas importadas.
        </p>
      )}
      {sincronizar.isError && (
        <p className="mt-2 text-sm text-red-600">
          {sincronizar.error instanceof Error ? sincronizar.error.message : "Não foi possível sincronizar."}
        </p>
      )}

      {mostrarForm && (
        <form onSubmit={handleSubmit((values) => criar.mutate(values))} className="cartao mt-4 grid max-w-xl gap-3">
          <div>
            <input placeholder="Órgão" className="campo" {...register("orgao")} />
            {errors.orgao && <p className="text-sm text-red-600">{errors.orgao.message}</p>}
          </div>
          <div>
            <textarea placeholder="Objeto da licitação" rows={3} className="campo" {...register("objeto")} />
            {errors.objeto && <p className="text-sm text-red-600">{errors.objeto.message}</p>}
          </div>
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            <input placeholder="Modalidade" className="campo" {...register("modalidade")} />
            <input placeholder="Número do processo" className="campo" {...register("numeroProcesso")} />
          </div>
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            <input placeholder="UF" maxLength={2} className="campo" {...register("uf")} />
            <input placeholder="Valor estimado" type="number" step="0.01" className="campo" {...register("valorEstimado")} />
          </div>
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            <label className="text-sm">
              Abertura das propostas
              <input type="datetime-local" className="campo mt-1" {...register("dataAberturaProposta")} />
            </label>
            <label className="text-sm">
              Encerramento das propostas
              <input type="datetime-local" className="campo mt-1" {...register("dataEncerramentoProposta")} />
            </label>
          </div>
          <input placeholder="Link do edital" className="campo" {...register("linkEdital")} />
          <textarea placeholder="Observações (opcional)" rows={2} className="campo" {...register("observacoes")} />
          {criar.isError && <p className="text-sm text-red-600">Não foi possível salvar a licitação.</p>}
          <button type="submit" disabled={criar.isPending} className="botao-destaque">
            {criar.isPending ? "Salvando..." : "Salvar licitação"}
          </button>
        </form>
      )}

      <div className="mt-4 flex flex-wrap gap-2">
        <button
          onClick={() => setFiltro("TODAS")}
          className={`badge border ${filtro === "TODAS" ? "border-mhr-navy bg-mhr-navy text-white" : "border-slate-200 text-slate-600"}`}
        >
          Todas ({todas.length})
        </button>
        {OPCOES_STATUS.map((status) => (
          <button
            key={status}
            onClick={() => setFiltro(status)}
            className={`badge border ${filtro === status ? "border-mhr-navy bg-mhr-navy text-white" : `border-transparent ${ESTILO_BADGE[status]}`}`}
          >
            {ROTULO_STATUS[status]} ({todas.filter((l) => l.status === status).length})
          </button>
        ))}
      </div>

      <div className="mt-4 flex flex-col gap-3">
        {listaFiltrada.length === 0 && <p className="text-sm text-slate-500">Nenhuma licitação encontrada com esse filtro.</p>}
        {listaFiltrada.map((licitacao) => (
          <CartaoLicitacao key={licitacao.id} licitacao={licitacao} responsaveis={responsaveis} />
        ))}
      </div>
    </div>
  );
}
