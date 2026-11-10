import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { apiFetch } from "../lib/api";
import CampoImagem from "../components/CampoImagem";

/** Espelha o MembroEquipeDto do backend (br.com.mhr.backend.equipe). */
interface MembroEquipe {
  id: string;
  nome: string;
  cargo?: string;
  fotoUrl?: string;
  bio?: string;
  ordemExibicao: number;
  publicado: boolean;
}

const membroSchema = z.object({
  nome: z.string().min(2),
  cargo: z.string().optional(),
  fotoUrl: z.string().optional(),
  bio: z.string().optional(),
  ordemExibicao: z.coerce.number().int().min(0),
  publicado: z.boolean(),
});

type MembroFormValues = z.infer<typeof membroSchema>;

/** Integrantes da equipe exibidos na página Sobre do site. */
export default function EquipePage() {
  const queryClient = useQueryClient();
  const [mostrarForm, setMostrarForm] = useState(false);
  const [editando, setEditando] = useState<string | null>(null);

  const { data, isLoading, error } = useQuery({
    queryKey: ["admin", "equipe"],
    queryFn: () => apiFetch<MembroEquipe[]>("/api/admin/equipe"),
  });

  const {
    register,
    handleSubmit,
    reset,
    watch,
    setValue,
    formState: { errors },
  } = useForm<MembroFormValues>({
    resolver: zodResolver(membroSchema),
    defaultValues: { ordemExibicao: 0, publicado: true },
  });

  function fecharForm() {
    reset({ nome: "", cargo: "", fotoUrl: "", bio: "", ordemExibicao: 0, publicado: true });
    setEditando(null);
    setMostrarForm(false);
  }

  function editar(membro: MembroEquipe) {
    reset(membro);
    setEditando(membro.id);
    setMostrarForm(true);
  }

  const criarMembro = useMutation({
    mutationFn: (values: MembroFormValues) =>
      apiFetch<MembroEquipe>("/api/admin/equipe", { method: "POST", body: JSON.stringify(values) }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "equipe"] });
      fecharForm();
    },
  });

  const atualizarMembro = useMutation({
    mutationFn: (values: MembroFormValues) =>
      apiFetch<MembroEquipe>(`/api/admin/equipe/${editando}`, { method: "PUT", body: JSON.stringify(values) }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "equipe"] });
      fecharForm();
    },
  });

  const salvando = criarMembro.isPending || atualizarMembro.isPending;
  const erroAoSalvar = criarMembro.isError || atualizarMembro.isError;

  const excluirMembro = useMutation({
    mutationFn: (id: string) => apiFetch<void>(`/api/admin/equipe/${id}`, { method: "DELETE" }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["admin", "equipe"] }),
  });

  if (isLoading) return <p>Carregando equipe...</p>;
  if (error) return <p className="text-red-600">Erro ao carregar a equipe.</p>;

  return (
    <div>
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-mhr-navy">Equipe</h1>
        <button onClick={() => (mostrarForm ? fecharForm() : setMostrarForm(true))} className="botao-primario">
          {mostrarForm ? "Cancelar" : "Novo integrante"}
        </button>
      </div>

      {mostrarForm && (
        <form
          onSubmit={handleSubmit((values) => (editando ? atualizarMembro.mutate(values) : criarMembro.mutate(values)))}
          className="cartao mt-4 grid max-w-xl gap-3"
        >
          <div>
            <input placeholder="Nome" className="campo" {...register("nome")} />
            {errors.nome && <p className="text-sm text-red-600">{errors.nome.message}</p>}
          </div>
          <input placeholder="Cargo (opcional)" className="campo" {...register("cargo")} />
          <textarea placeholder="Bio (opcional)" rows={3} className="campo" {...register("bio")} />
          <input placeholder="URL da foto (opcional)" className="campo" {...register("fotoUrl")} />
          <CampoImagem valorAtual={watch("fotoUrl")} aoEnviar={(url) => setValue("fotoUrl", url, { shouldDirty: true })} />
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
          {erroAoSalvar && <p className="text-sm text-red-600">Não foi possível salvar o integrante.</p>}
          <button type="submit" disabled={salvando} className="botao-destaque">
            {salvando ? "Salvando..." : editando ? "Salvar alterações" : "Salvar integrante"}
          </button>
        </form>
      )}

      <div className="cartao mt-6 overflow-x-auto">
        {data?.length === 0 ? (
          <p className="text-sm text-slate-500">Nenhum integrante cadastrado ainda.</p>
        ) : (
          <table className="w-full border-collapse text-left text-sm">
            <thead>
              <tr className="border-b border-slate-200">
                <th className="py-2">Nome</th>
                <th className="py-2">Cargo</th>
                <th className="py-2">Ordem</th>
                <th className="py-2">Publicado</th>
                <th className="py-2" />
              </tr>
            </thead>
            <tbody>
              {data?.map((membro) => (
                <tr key={membro.id} className="border-b border-slate-100 last:border-0">
                  <td className="py-2">{membro.nome}</td>
                  <td className="py-2">{membro.cargo ?? "—"}</td>
                  <td className="py-2">{membro.ordemExibicao}</td>
                  <td className="py-2">
                    <span className={`badge ${membro.publicado ? "bg-green-100 text-green-700" : "bg-slate-100 text-slate-600"}`}>
                      {membro.publicado ? "Sim" : "Não"}
                    </span>
                  </td>
                  <td className="py-2 text-right">
                    <button onClick={() => editar(membro)} className="mr-3 text-sm text-mhr-gold hover:underline">
                      Editar
                    </button>
                    <button onClick={() => excluirMembro.mutate(membro.id)} className="text-sm text-red-600 hover:underline">
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
