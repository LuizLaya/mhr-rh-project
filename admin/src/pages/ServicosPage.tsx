import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { apiFetch } from "../lib/api";

/** Espelha o ServicoDto do backend (br.com.mhr.backend.servico). */
interface Servico {
  id: string;
  titulo: string;
  slug: string;
  resumo: string;
  conteudo: string;
  icone?: string;
  ordemExibicao: number;
  publicado: boolean;
}

const servicoSchema = z.object({
  titulo: z.string().min(2),
  slug: z.string().min(2),
  resumo: z.string().min(5),
  conteudo: z.string().min(10),
  icone: z.string().optional(),
  ordemExibicao: z.coerce.number().int().min(0),
  publicado: z.boolean(),
});

type ServicoFormValues = z.infer<typeof servicoSchema>;

export default function ServicosPage() {
  const queryClient = useQueryClient();
  const [mostrarForm, setMostrarForm] = useState(false);
  const [editando, setEditando] = useState<string | null>(null);
  const [busca, setBusca] = useState("");

  const { data, isLoading, error } = useQuery({
    queryKey: ["admin", "servicos"],
    queryFn: () => apiFetch<Servico[]>("/api/admin/servicos"),
  });

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<ServicoFormValues>({
    resolver: zodResolver(servicoSchema),
    defaultValues: { ordemExibicao: 0, publicado: true },
  });

  function fecharForm() {
    reset({ titulo: "", slug: "", resumo: "", conteudo: "", icone: "", ordemExibicao: 0, publicado: true });
    setEditando(null);
    setMostrarForm(false);
  }

  function editar(servico: Servico) {
    reset(servico);
    setEditando(servico.id);
    setMostrarForm(true);
  }

  const criarServico = useMutation({
    mutationFn: (values: ServicoFormValues) =>
      apiFetch<Servico>("/api/admin/servicos", { method: "POST", body: JSON.stringify(values) }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "servicos"] });
      fecharForm();
    },
  });

  const atualizarServico = useMutation({
    mutationFn: (values: ServicoFormValues) =>
      apiFetch<Servico>(`/api/admin/servicos/${editando}`, { method: "PUT", body: JSON.stringify(values) }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "servicos"] });
      fecharForm();
    },
  });

  const salvando = criarServico.isPending || atualizarServico.isPending;
  const erroAoSalvar = criarServico.isError || atualizarServico.isError;

  const excluirServico = useMutation({
    mutationFn: (id: string) => apiFetch<void>(`/api/admin/servicos/${id}`, { method: "DELETE" }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["admin", "servicos"] }),
  });

  if (isLoading) return <p>Carregando serviços...</p>;
  if (error) return <p className="text-red-600">Erro ao carregar serviços.</p>;

  const buscaEmMinusculas = busca.trim().toLowerCase();
  const dadosFiltrados = (data ?? []).filter(
    (servico) => !buscaEmMinusculas || servico.titulo.toLowerCase().includes(buscaEmMinusculas)
  );

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-2xl font-bold text-mhr-navy">Serviços</h1>
        <div className="flex items-center gap-3">
          <input
            value={busca}
            onChange={(e) => setBusca(e.target.value)}
            placeholder="Buscar por título..."
            className="campo w-56"
          />
          <button onClick={() => (mostrarForm ? fecharForm() : setMostrarForm(true))} className="botao-primario">
            {mostrarForm ? "Cancelar" : "Novo serviço"}
          </button>
        </div>
      </div>

      {mostrarForm && (
        <form
          onSubmit={handleSubmit((values) => (editando ? atualizarServico.mutate(values) : criarServico.mutate(values)))}
          className="cartao mt-4 grid max-w-xl gap-3"
        >
          <div>
            <input placeholder="Título" className="campo" {...register("titulo")} />
            {errors.titulo && <p className="text-sm text-red-600">{errors.titulo.message}</p>}
          </div>
          <div>
            <input placeholder="Slug" className="campo" {...register("slug")} />
            {errors.slug && <p className="text-sm text-red-600">{errors.slug.message}</p>}
          </div>
          <div>
            <input placeholder="Resumo" className="campo" {...register("resumo")} />
            {errors.resumo && <p className="text-sm text-red-600">{errors.resumo.message}</p>}
          </div>
          <div>
            <textarea placeholder="Conteúdo completo" rows={4} className="campo" {...register("conteudo")} />
            {errors.conteudo && <p className="text-sm text-red-600">{errors.conteudo.message}</p>}
          </div>
          <input placeholder="Ícone (opcional)" className="campo" {...register("icone")} />
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
          {erroAoSalvar && <p className="text-sm text-red-600">Não foi possível salvar o serviço.</p>}
          <button type="submit" disabled={salvando} className="botao-destaque">
            {salvando ? "Salvando..." : editando ? "Salvar alterações" : "Salvar serviço"}
          </button>
        </form>
      )}

      <div className="cartao mt-6 overflow-x-auto">
        {dadosFiltrados.length === 0 ? (
          <p className="text-sm text-slate-500">
            {busca ? "Nenhum serviço encontrado para essa busca." : "Nenhum serviço cadastrado ainda."}
          </p>
        ) : (
          <table className="w-full border-collapse text-left text-sm">
            <thead>
              <tr className="border-b border-slate-200">
                <th className="py-2">Título</th>
                <th className="py-2">Ordem</th>
                <th className="py-2">Publicado</th>
                <th className="py-2" />
              </tr>
            </thead>
            <tbody>
              {dadosFiltrados.map((servico) => (
                <tr key={servico.id} className="border-b border-slate-100 last:border-0">
                  <td className="py-2">{servico.titulo}</td>
                  <td className="py-2">{servico.ordemExibicao}</td>
                  <td className="py-2">
                    <span className={`badge ${servico.publicado ? "bg-green-100 text-green-700" : "bg-slate-100 text-slate-600"}`}>
                      {servico.publicado ? "Sim" : "Não"}
                    </span>
                  </td>
                  <td className="py-2 text-right">
                    <button onClick={() => editar(servico)} className="mr-3 text-sm text-mhr-gold hover:underline">
                      Editar
                    </button>
                    <button onClick={() => excluirServico.mutate(servico.id)} className="text-sm text-red-600 hover:underline">
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
