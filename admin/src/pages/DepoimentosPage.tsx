import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { apiFetch } from "../lib/api";
import CampoImagem from "../components/CampoImagem";
import type { Cliente } from "./ClientesPage";

/** Espelha o DepoimentoDto do backend (br.com.mhr.backend.depoimento). */
interface Depoimento {
  id: string;
  nomeAutor: string;
  cargoAutor?: string;
  nomeEmpresa?: string;
  conteudo: string;
  urlFoto?: string;
  ordemExibicao: number;
  publicado: boolean;
  clienteId?: string;
}

const depoimentoSchema = z.object({
  nomeAutor: z.string().min(2),
  cargoAutor: z.string().optional(),
  nomeEmpresa: z.string().optional(),
  conteudo: z.string().min(10),
  urlFoto: z.string().optional(),
  ordemExibicao: z.coerce.number().int().min(0),
  publicado: z.boolean(),
  clienteId: z.string().optional(),
});

type DepoimentoFormValues = z.infer<typeof depoimentoSchema>;

export default function DepoimentosPage() {
  const queryClient = useQueryClient();
  const [mostrarForm, setMostrarForm] = useState(false);
  const [editando, setEditando] = useState<string | null>(null);
  const [busca, setBusca] = useState("");

  const { data, isLoading, error } = useQuery({
    queryKey: ["admin", "depoimentos"],
    queryFn: () => apiFetch<Depoimento[]>("/api/admin/depoimentos"),
  });

  const { data: clientes } = useQuery({
    queryKey: ["admin", "clientes"],
    queryFn: () => apiFetch<Cliente[]>("/api/admin/clientes"),
  });

  const {
    register,
    handleSubmit,
    reset,
    watch,
    setValue,
    formState: { errors },
  } = useForm<DepoimentoFormValues>({
    resolver: zodResolver(depoimentoSchema),
    defaultValues: { ordemExibicao: 0, publicado: true },
  });

  function fecharForm() {
    reset({ nomeAutor: "", cargoAutor: "", nomeEmpresa: "", conteudo: "", urlFoto: "", ordemExibicao: 0, publicado: true, clienteId: "" });
    setEditando(null);
    setMostrarForm(false);
  }

  function editar(depoimento: Depoimento) {
    reset({ ...depoimento, clienteId: depoimento.clienteId ?? "" });
    setEditando(depoimento.id);
    setMostrarForm(true);
  }

  const criarDepoimento = useMutation({
    mutationFn: (values: DepoimentoFormValues) =>
      apiFetch<Depoimento>("/api/admin/depoimentos", {
        method: "POST",
        body: JSON.stringify({ ...values, clienteId: values.clienteId || null }),
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "depoimentos"] });
      fecharForm();
    },
  });

  const atualizarDepoimento = useMutation({
    mutationFn: (values: DepoimentoFormValues) =>
      apiFetch<Depoimento>(`/api/admin/depoimentos/${editando}`, {
        method: "PUT",
        body: JSON.stringify({ ...values, clienteId: values.clienteId || null }),
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "depoimentos"] });
      fecharForm();
    },
  });

  const salvando = criarDepoimento.isPending || atualizarDepoimento.isPending;
  const erroAoSalvar = criarDepoimento.isError || atualizarDepoimento.isError;

  const excluirDepoimento = useMutation({
    mutationFn: (id: string) => apiFetch<void>(`/api/admin/depoimentos/${id}`, { method: "DELETE" }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["admin", "depoimentos"] }),
  });

  if (isLoading) return <p>Carregando depoimentos...</p>;
  if (error) return <p className="text-red-600">Erro ao carregar depoimentos.</p>;

  const buscaEmMinusculas = busca.trim().toLowerCase();
  const dadosFiltrados = (data ?? []).filter(
    (depoimento) =>
      !buscaEmMinusculas ||
      depoimento.nomeAutor.toLowerCase().includes(buscaEmMinusculas) ||
      (depoimento.nomeEmpresa ?? "").toLowerCase().includes(buscaEmMinusculas)
  );

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-2xl font-bold text-mhr-navy">Depoimentos</h1>
        <div className="flex items-center gap-3">
          <input
            value={busca}
            onChange={(e) => setBusca(e.target.value)}
            placeholder="Buscar por autor ou empresa..."
            className="campo w-56"
          />
          <button onClick={() => (mostrarForm ? fecharForm() : setMostrarForm(true))} className="botao-primario">
            {mostrarForm ? "Cancelar" : "Novo depoimento"}
          </button>
        </div>
      </div>

      {mostrarForm && (
        <form
          onSubmit={handleSubmit((values) => (editando ? atualizarDepoimento.mutate(values) : criarDepoimento.mutate(values)))}
          className="cartao mt-4 grid max-w-xl gap-3"
        >
          <div>
            <input placeholder="Nome do autor" className="campo" {...register("nomeAutor")} />
            {errors.nomeAutor && <p className="text-sm text-red-600">{errors.nomeAutor.message}</p>}
          </div>
          <input placeholder="Cargo (opcional)" className="campo" {...register("cargoAutor")} />
          <input placeholder="Empresa (opcional)" className="campo" {...register("nomeEmpresa")} />
          <select className="campo" {...register("clienteId")}>
            <option value="">Sem cliente vinculado</option>
            {clientes?.map((cliente) => (
              <option key={cliente.id} value={cliente.id}>{cliente.nomeEmpresa}</option>
            ))}
          </select>
          <div>
            <textarea placeholder="Depoimento" rows={4} className="campo" {...register("conteudo")} />
            {errors.conteudo && <p className="text-sm text-red-600">{errors.conteudo.message}</p>}
          </div>
          <input placeholder="URL da foto (opcional)" className="campo" {...register("urlFoto")} />
          <CampoImagem valorAtual={watch("urlFoto")} aoEnviar={(url) => setValue("urlFoto", url, { shouldDirty: true })} />
          <div className="flex items-center gap-4">
            <label className="flex items-center gap-2 text-sm">
              Ordem
              <input type="number" className="campo w-20" {...register("ordemExibicao")} />
            </label>
            <label className="flex items-center gap-2 text-sm">
              <input type="checkbox" {...register("publicado")} />
              Publicado
            </label>
          </div>
          {erroAoSalvar && <p className="text-sm text-red-600">Não foi possível salvar o depoimento.</p>}
          <button type="submit" disabled={salvando} className="botao-destaque">
            {salvando ? "Salvando..." : editando ? "Salvar alterações" : "Salvar depoimento"}
          </button>
        </form>
      )}

      <div className="cartao mt-6 overflow-x-auto">
        {dadosFiltrados.length === 0 ? (
          <p className="text-sm text-slate-500">
            {busca ? "Nenhum depoimento encontrado para essa busca." : "Nenhum depoimento cadastrado ainda."}
          </p>
        ) : (
          <table className="w-full border-collapse text-left text-sm">
            <thead>
              <tr className="border-b border-slate-200">
                <th className="py-2">Autor</th>
                <th className="py-2">Empresa</th>
                <th className="py-2">Cliente</th>
                <th className="py-2">Publicado</th>
                <th className="py-2" />
              </tr>
            </thead>
            <tbody>
              {dadosFiltrados.map((depoimento) => (
                <tr key={depoimento.id} className="border-b border-slate-100 last:border-0">
                  <td className="py-2">{depoimento.nomeAutor}</td>
                  <td className="py-2">{depoimento.nomeEmpresa ?? "—"}</td>
                  <td className="py-2">
                    {clientes?.find((cliente) => cliente.id === depoimento.clienteId)?.nomeEmpresa ?? "—"}
                  </td>
                  <td className="py-2">
                    <span className={`badge ${depoimento.publicado ? "bg-green-100 text-green-700" : "bg-slate-100 text-slate-600"}`}>
                      {depoimento.publicado ? "Sim" : "Não"}
                    </span>
                  </td>
                  <td className="py-2 text-right">
                    <button onClick={() => editar(depoimento)} className="mr-3 text-sm text-mhr-gold hover:underline">
                      Editar
                    </button>
                    <button onClick={() => excluirDepoimento.mutate(depoimento.id)} className="text-sm text-red-600 hover:underline">
                      Excluir
                    </button>
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
