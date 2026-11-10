import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { apiFetch } from "../lib/api";

/** Espelha o VagaDto do backend (br.com.mhr.backend.vaga). */
interface Vaga {
  id: string;
  titulo: string;
  slug: string;
  descricao: string;
  cidade: string;
  modeloTrabalho: "PRESENCIAL" | "HIBRIDO" | "REMOTO";
  status: "ABERTA" | "ENCERRADA";
}

const vagaSchema = z.object({
  titulo: z.string().min(2),
  slug: z.string().min(2),
  descricao: z.string().min(10),
  cidade: z.string().min(2),
  modeloTrabalho: z.enum(["PRESENCIAL", "HIBRIDO", "REMOTO"]),
  status: z.enum(["ABERTA", "ENCERRADA"]),
});

type VagaFormValues = z.infer<typeof vagaSchema>;

export default function VagasPage() {
  const queryClient = useQueryClient();
  const [mostrarForm, setMostrarForm] = useState(false);
  const [editando, setEditando] = useState<string | null>(null);
  const [busca, setBusca] = useState("");

  const { data, isLoading, error } = useQuery({
    queryKey: ["admin", "vagas"],
    queryFn: () => apiFetch<Vaga[]>("/api/admin/vagas"),
  });

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<VagaFormValues>({
    resolver: zodResolver(vagaSchema),
    defaultValues: { modeloTrabalho: "PRESENCIAL", status: "ABERTA" },
  });

  function fecharForm() {
    reset({ titulo: "", slug: "", descricao: "", cidade: "", modeloTrabalho: "PRESENCIAL", status: "ABERTA" });
    setEditando(null);
    setMostrarForm(false);
  }

  function editar(vaga: Vaga) {
    reset(vaga);
    setEditando(vaga.id);
    setMostrarForm(true);
  }

  const criarVaga = useMutation({
    mutationFn: (values: VagaFormValues) =>
      apiFetch<Vaga>("/api/admin/vagas", { method: "POST", body: JSON.stringify(values) }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "vagas"] });
      fecharForm();
    },
  });

  const atualizarVaga = useMutation({
    mutationFn: (values: VagaFormValues) =>
      apiFetch<Vaga>(`/api/admin/vagas/${editando}`, { method: "PUT", body: JSON.stringify(values) }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "vagas"] });
      fecharForm();
    },
  });

  const salvando = criarVaga.isPending || atualizarVaga.isPending;
  const erroAoSalvar = criarVaga.isError || atualizarVaga.isError;

  const excluirVaga = useMutation({
    mutationFn: (id: string) => apiFetch<void>(`/api/admin/vagas/${id}`, { method: "DELETE" }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["admin", "vagas"] }),
  });

  if (isLoading) return <p>Carregando vagas...</p>;
  if (error) return <p className="text-red-600">Erro ao carregar vagas (verifique se o login ainda é válido).</p>;

  const buscaEmMinusculas = busca.trim().toLowerCase();
  const dadosFiltrados = (data ?? []).filter(
    (vaga) => !buscaEmMinusculas || vaga.titulo.toLowerCase().includes(buscaEmMinusculas) || vaga.cidade.toLowerCase().includes(buscaEmMinusculas)
  );

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-2xl font-bold text-mhr-navy">Vagas</h1>
        <div className="flex items-center gap-3">
          <input
            value={busca}
            onChange={(e) => setBusca(e.target.value)}
            placeholder="Buscar por título ou cidade..."
            className="campo w-56"
          />
          <button onClick={() => (mostrarForm ? fecharForm() : setMostrarForm(true))} className="botao-primario">
            {mostrarForm ? "Cancelar" : "Nova vaga"}
          </button>
        </div>
      </div>

      {mostrarForm && (
        <form
          onSubmit={handleSubmit((values) => (editando ? atualizarVaga.mutate(values) : criarVaga.mutate(values)))}
          className="cartao mt-4 grid max-w-xl gap-3"
        >
          <div>
            <input placeholder="Título" className="campo" {...register("titulo")} />
            {errors.titulo && <p className="text-sm text-red-600">{errors.titulo.message}</p>}
          </div>
          <div>
            <input placeholder="Slug (ex.: analista-rh-pleno)" className="campo" {...register("slug")} />
            {errors.slug && <p className="text-sm text-red-600">{errors.slug.message}</p>}
          </div>
          <div>
            <textarea placeholder="Descrição" rows={4} className="campo" {...register("descricao")} />
            {errors.descricao && <p className="text-sm text-red-600">{errors.descricao.message}</p>}
          </div>
          <div>
            <input placeholder="Cidade" className="campo" {...register("cidade")} />
            {errors.cidade && <p className="text-sm text-red-600">{errors.cidade.message}</p>}
          </div>
          <div className="flex gap-3">
            <select className="campo" {...register("modeloTrabalho")}>
              <option value="PRESENCIAL">Presencial</option>
              <option value="HIBRIDO">Híbrido</option>
              <option value="REMOTO">Remoto</option>
            </select>
            <select className="campo" {...register("status")}>
              <option value="ABERTA">Aberta</option>
              <option value="ENCERRADA">Encerrada</option>
            </select>
          </div>
          {erroAoSalvar && <p className="text-sm text-red-600">Não foi possível salvar a vaga.</p>}
          <button type="submit" disabled={salvando} className="botao-destaque">
            {salvando ? "Salvando..." : editando ? "Salvar alterações" : "Salvar vaga"}
          </button>
        </form>
      )}

      <div className="cartao mt-6 overflow-x-auto">
        {dadosFiltrados.length === 0 ? (
          <p className="text-sm text-slate-500">
            {busca ? "Nenhuma vaga encontrada para essa busca." : "Nenhuma vaga cadastrada ainda."}
          </p>
        ) : (
          <table className="w-full border-collapse text-left text-sm">
            <thead>
              <tr className="border-b border-slate-200">
                <th className="py-2">Título</th>
                <th className="py-2">Cidade</th>
                <th className="py-2">Modelo</th>
                <th className="py-2">Status</th>
                <th className="py-2" />
              </tr>
            </thead>
            <tbody>
              {dadosFiltrados.map((vaga) => (
                <tr key={vaga.id} className="border-b border-slate-100 last:border-0">
                  <td className="py-2">{vaga.titulo}</td>
                  <td className="py-2">{vaga.cidade}</td>
                  <td className="py-2">{vaga.modeloTrabalho}</td>
                  <td className="py-2">
                    <span className={`badge ${vaga.status === "ABERTA" ? "bg-green-100 text-green-700" : "bg-slate-100 text-slate-600"}`}>
                      {vaga.status === "ABERTA" ? "Aberta" : "Encerrada"}
                    </span>
                  </td>
                  <td className="py-2 text-right">
                    <button onClick={() => editar(vaga)} className="mr-3 text-sm text-mhr-gold hover:underline">
                      Editar
                    </button>
                    <button onClick={() => excluirVaga.mutate(vaga.id)} className="text-sm text-red-600 hover:underline">
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
