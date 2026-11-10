import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { apiFetch } from "../lib/api";

/** Espelha o FaqDto do backend (br.com.mhr.backend.faq). */
interface Faq {
  id: string;
  pergunta: string;
  resposta: string;
  ordemExibicao: number;
  publicado: boolean;
}

const faqSchema = z.object({
  pergunta: z.string().min(2),
  resposta: z.string().min(2),
  ordemExibicao: z.coerce.number().int().min(0),
  publicado: z.boolean(),
});

type FaqFormValues = z.infer<typeof faqSchema>;

/** Perguntas frequentes exibidas na home do site. */
export default function FaqPage() {
  const queryClient = useQueryClient();
  const [mostrarForm, setMostrarForm] = useState(false);
  const [editando, setEditando] = useState<string | null>(null);

  const { data, isLoading, error } = useQuery({
    queryKey: ["admin", "faq"],
    queryFn: () => apiFetch<Faq[]>("/api/admin/faq"),
  });

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FaqFormValues>({
    resolver: zodResolver(faqSchema),
    defaultValues: { ordemExibicao: 0, publicado: true },
  });

  function fecharForm() {
    reset({ pergunta: "", resposta: "", ordemExibicao: 0, publicado: true });
    setEditando(null);
    setMostrarForm(false);
  }

  function editar(faq: Faq) {
    reset(faq);
    setEditando(faq.id);
    setMostrarForm(true);
  }

  const criarFaq = useMutation({
    mutationFn: (values: FaqFormValues) =>
      apiFetch<Faq>("/api/admin/faq", { method: "POST", body: JSON.stringify(values) }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "faq"] });
      fecharForm();
    },
  });

  const atualizarFaq = useMutation({
    mutationFn: (values: FaqFormValues) =>
      apiFetch<Faq>(`/api/admin/faq/${editando}`, { method: "PUT", body: JSON.stringify(values) }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "faq"] });
      fecharForm();
    },
  });

  const salvando = criarFaq.isPending || atualizarFaq.isPending;
  const erroAoSalvar = criarFaq.isError || atualizarFaq.isError;

  const excluirFaq = useMutation({
    mutationFn: (id: string) => apiFetch<void>(`/api/admin/faq/${id}`, { method: "DELETE" }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["admin", "faq"] }),
  });

  if (isLoading) return <p>Carregando perguntas frequentes...</p>;
  if (error) return <p className="text-red-600">Erro ao carregar as perguntas frequentes.</p>;

  return (
    <div>
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-mhr-navy">FAQ</h1>
        <button onClick={() => (mostrarForm ? fecharForm() : setMostrarForm(true))} className="botao-primario">
          {mostrarForm ? "Cancelar" : "Nova pergunta"}
        </button>
      </div>

      {mostrarForm && (
        <form
          onSubmit={handleSubmit((values) => (editando ? atualizarFaq.mutate(values) : criarFaq.mutate(values)))}
          className="cartao mt-4 grid max-w-xl gap-3"
        >
          <div>
            <input placeholder="Pergunta" className="campo" {...register("pergunta")} />
            {errors.pergunta && <p className="text-sm text-red-600">{errors.pergunta.message}</p>}
          </div>
          <div>
            <textarea placeholder="Resposta" rows={4} className="campo" {...register("resposta")} />
            {errors.resposta && <p className="text-sm text-red-600">{errors.resposta.message}</p>}
          </div>
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
          {erroAoSalvar && <p className="text-sm text-red-600">Não foi possível salvar a pergunta.</p>}
          <button type="submit" disabled={salvando} className="botao-destaque">
            {salvando ? "Salvando..." : editando ? "Salvar alterações" : "Salvar pergunta"}
          </button>
        </form>
      )}

      <div className="cartao mt-6 overflow-x-auto">
        {data?.length === 0 ? (
          <p className="text-sm text-slate-500">Nenhuma pergunta cadastrada ainda.</p>
        ) : (
          <table className="w-full border-collapse text-left text-sm">
            <thead>
              <tr className="border-b border-slate-200">
                <th className="py-2">Pergunta</th>
                <th className="py-2">Ordem</th>
                <th className="py-2">Publicado</th>
                <th className="py-2" />
              </tr>
            </thead>
            <tbody>
              {data?.map((faq) => (
                <tr key={faq.id} className="border-b border-slate-100 last:border-0">
                  <td className="py-2">{faq.pergunta}</td>
                  <td className="py-2">{faq.ordemExibicao}</td>
                  <td className="py-2">
                    <span className={`badge ${faq.publicado ? "bg-green-100 text-green-700" : "bg-slate-100 text-slate-600"}`}>
                      {faq.publicado ? "Sim" : "Não"}
                    </span>
                  </td>
                  <td className="py-2 text-right">
                    <button onClick={() => editar(faq)} className="mr-3 text-sm text-mhr-gold hover:underline">
                      Editar
                    </button>
                    <button onClick={() => excluirFaq.mutate(faq.id)} className="text-sm text-red-600 hover:underline">
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
