import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { apiFetch } from "../lib/api";
import CampoImagem from "../components/CampoImagem";

/** Espelha o PostBlogDto do backend (br.com.mhr.backend.blog). */
interface PostBlog {
  id: string;
  titulo: string;
  slug: string;
  resumo: string;
  conteudo: string;
  urlImagemCapa?: string;
  categoria?: string;
  publicado: boolean;
}

const postSchema = z.object({
  titulo: z.string().min(2),
  slug: z.string().min(2),
  resumo: z.string().min(5),
  conteudo: z.string().min(10),
  urlImagemCapa: z.string().optional(),
  categoria: z.string().optional(),
  publicado: z.boolean(),
});

type PostFormValues = z.infer<typeof postSchema>;

export default function PostsBlogPage() {
  const queryClient = useQueryClient();
  const [mostrarForm, setMostrarForm] = useState(false);
  const [editando, setEditando] = useState<string | null>(null);
  const [busca, setBusca] = useState("");

  const { data, isLoading, error } = useQuery({
    queryKey: ["admin", "posts-blog"],
    queryFn: () => apiFetch<PostBlog[]>("/api/admin/posts-blog"),
  });

  const {
    register,
    handleSubmit,
    reset,
    watch,
    setValue,
    formState: { errors },
  } = useForm<PostFormValues>({
    resolver: zodResolver(postSchema),
    defaultValues: { publicado: false },
  });

  function fecharForm() {
    reset({ titulo: "", slug: "", resumo: "", conteudo: "", urlImagemCapa: "", categoria: "", publicado: false });
    setEditando(null);
    setMostrarForm(false);
  }

  function editar(post: PostBlog) {
    reset(post);
    setEditando(post.id);
    setMostrarForm(true);
  }

  const criarPost = useMutation({
    mutationFn: (values: PostFormValues) =>
      apiFetch<PostBlog>("/api/admin/posts-blog", { method: "POST", body: JSON.stringify(values) }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "posts-blog"] });
      fecharForm();
    },
  });

  const atualizarPost = useMutation({
    mutationFn: (values: PostFormValues) =>
      apiFetch<PostBlog>(`/api/admin/posts-blog/${editando}`, { method: "PUT", body: JSON.stringify(values) }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "posts-blog"] });
      fecharForm();
    },
  });

  const salvando = criarPost.isPending || atualizarPost.isPending;
  const erroAoSalvar = criarPost.isError || atualizarPost.isError;

  const excluirPost = useMutation({
    mutationFn: (id: string) => apiFetch<void>(`/api/admin/posts-blog/${id}`, { method: "DELETE" }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["admin", "posts-blog"] }),
  });

  if (isLoading) return <p>Carregando posts...</p>;
  if (error) return <p className="text-red-600">Erro ao carregar posts.</p>;

  const buscaEmMinusculas = busca.trim().toLowerCase();
  const dadosFiltrados = (data ?? []).filter(
    (post) =>
      !buscaEmMinusculas ||
      post.titulo.toLowerCase().includes(buscaEmMinusculas) ||
      (post.categoria ?? "").toLowerCase().includes(buscaEmMinusculas)
  );

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-2xl font-bold text-mhr-navy">Blog</h1>
        <div className="flex items-center gap-3">
          <input
            value={busca}
            onChange={(e) => setBusca(e.target.value)}
            placeholder="Buscar por título ou categoria..."
            className="campo w-56"
          />
          <button onClick={() => (mostrarForm ? fecharForm() : setMostrarForm(true))} className="botao-primario">
            {mostrarForm ? "Cancelar" : "Novo post"}
          </button>
        </div>
      </div>

      {mostrarForm && (
        <form
          onSubmit={handleSubmit((values) => (editando ? atualizarPost.mutate(values) : criarPost.mutate(values)))}
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
            <textarea placeholder="Conteúdo completo" rows={6} className="campo" {...register("conteudo")} />
            {errors.conteudo && <p className="text-sm text-red-600">{errors.conteudo.message}</p>}
          </div>
          <input placeholder="URL da imagem de capa (opcional)" className="campo" {...register("urlImagemCapa")} />
          <CampoImagem valorAtual={watch("urlImagemCapa")} aoEnviar={(url) => setValue("urlImagemCapa", url, { shouldDirty: true })} />
          <input placeholder="Categoria (opcional)" className="campo" {...register("categoria")} />
          <label className="flex items-center gap-2 text-sm">
            <input type="checkbox" {...register("publicado")} />
            Publicar agora
          </label>
          {erroAoSalvar && <p className="text-sm text-red-600">Não foi possível salvar o post.</p>}
          <button type="submit" disabled={salvando} className="botao-destaque">
            {salvando ? "Salvando..." : editando ? "Salvar alterações" : "Salvar post"}
          </button>
        </form>
      )}

      <div className="cartao mt-6 overflow-x-auto">
        {dadosFiltrados.length === 0 ? (
          <p className="text-sm text-slate-500">
            {busca ? "Nenhum post encontrado para essa busca." : "Nenhum post cadastrado ainda."}
          </p>
        ) : (
          <table className="w-full border-collapse text-left text-sm">
            <thead>
              <tr className="border-b border-slate-200">
                <th className="py-2">Título</th>
                <th className="py-2">Categoria</th>
                <th className="py-2">Publicado</th>
                <th className="py-2" />
              </tr>
            </thead>
            <tbody>
              {dadosFiltrados.map((post) => (
                <tr key={post.id} className="border-b border-slate-100 last:border-0">
                  <td className="py-2">{post.titulo}</td>
                  <td className="py-2">{post.categoria ?? "—"}</td>
                  <td className="py-2">
                    <span className={`badge ${post.publicado ? "bg-green-100 text-green-700" : "bg-slate-100 text-slate-600"}`}>
                      {post.publicado ? "Sim" : "Não"}
                    </span>
                  </td>
                  <td className="py-2 text-right">
                    <button onClick={() => editar(post)} className="mr-3 text-sm text-mhr-gold hover:underline">
                      Editar
                    </button>
                    <button onClick={() => excluirPost.mutate(post.id)} className="text-sm text-red-600 hover:underline">
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
