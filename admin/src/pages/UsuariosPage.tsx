import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Link } from "react-router";
import { z } from "zod";
import { apiFetch } from "../lib/api";

type Papel = "ADMIN" | "EDITOR";

/** Espelha o UsuarioDto do backend (br.com.mhr.backend.usuario). */
interface Usuario {
  id: string;
  nome: string;
  email: string;
  papel: Papel;
  ativo: boolean;
  senhaDefinida: boolean;
  criadoEm: string;
}

const convidarSchema = z.object({
  nome: z.string().min(2),
  email: z.string().email(),
  papel: z.enum(["ADMIN", "EDITOR"]),
});

type ConvidarFormValues = z.infer<typeof convidarSchema>;

/** Espelha o ConvidarUsuarioResponseDto do backend. */
interface ConvidarUsuarioResponse {
  usuario: Usuario;
  emailEnviado: boolean;
}

function Status({ usuario }: { usuario: Usuario }) {
  if (!usuario.ativo) return <span className="badge bg-red-100 text-red-700">Desativado</span>;
  if (!usuario.senhaDefinida) return <span className="badge bg-amber-100 text-amber-700">Convite pendente</span>;
  return <span className="badge bg-green-100 text-green-700">Ativo</span>;
}

/**
 * Gestão de quem acessa o painel admin. Só ADMIN enxerga esta tela (ver RotaAdmin em
 * App.tsx) — o backend também restringe por papel (UsuarioAdminController), então
 * esconder o link aqui é só UX; a segurança de verdade está no servidor.
 */
export default function UsuariosPage() {
  const queryClient = useQueryClient();
  const [mostrarForm, setMostrarForm] = useState(false);
  const [resultadoConvite, setResultadoConvite] = useState<{ email: string; emailEnviado: boolean } | null>(null);

  const { data, isLoading, error } = useQuery({
    queryKey: ["admin", "usuarios"],
    queryFn: () => apiFetch<Usuario[]>("/api/admin/usuarios"),
  });

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<ConvidarFormValues>({
    resolver: zodResolver(convidarSchema),
    defaultValues: { papel: "EDITOR" },
  });

  const convidar = useMutation({
    mutationFn: (values: ConvidarFormValues) =>
      apiFetch<ConvidarUsuarioResponse>("/api/admin/usuarios/convidar", { method: "POST", body: JSON.stringify(values) }),
    onSuccess: (resultado, values) => {
      queryClient.invalidateQueries({ queryKey: ["admin", "usuarios"] });
      reset();
      setMostrarForm(false);
      setResultadoConvite({ email: values.email, emailEnviado: resultado.emailEnviado });
    },
  });

  const atualizarAcesso = useMutation({
    mutationFn: ({ id, ativo }: { id: string; ativo: boolean }) =>
      apiFetch<Usuario>(`/api/admin/usuarios/${id}/acesso`, { method: "PATCH", body: JSON.stringify({ ativo }) }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["admin", "usuarios"] }),
  });

  if (isLoading) return <p>Carregando usuários...</p>;
  if (error) return <p className="text-red-600">Erro ao carregar usuários.</p>;

  return (
    <div>
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-mhr-navy">Usuários do painel</h1>
        <button onClick={() => setMostrarForm((v) => !v)} className="botao-primario">
          {mostrarForm ? "Cancelar" : "Convidar usuário"}
        </button>
      </div>

      {resultadoConvite && resultadoConvite.emailEnviado && (
        <p className="mt-4 rounded-lg bg-green-50 p-3 text-sm text-green-700">
          Convite enviado por e-mail para <strong>{resultadoConvite.email}</strong>.
        </p>
      )}
      {resultadoConvite && !resultadoConvite.emailEnviado && (
        <p className="mt-4 rounded-lg bg-amber-50 p-3 text-sm text-amber-800">
          O usuário <strong>{resultadoConvite.email}</strong> foi criado, mas o e-mail de convite{" "}
          <strong>não pôde ser enviado</strong> (provedor recusou ou não está configurado). Veja o link de ativação
          na aba <Link to="/logs" className="underline">Logs</Link> (categoria CONVITE) e repasse manualmente.
        </p>
      )}

      {mostrarForm && (
        <form onSubmit={handleSubmit((values) => convidar.mutate(values))} className="cartao mt-4 grid max-w-md gap-3">
          <div>
            <input placeholder="Nome" className="campo" {...register("nome")} />
            {errors.nome && <p className="text-sm text-red-600">{errors.nome.message}</p>}
          </div>
          <div>
            <input placeholder="E-mail" type="email" className="campo" {...register("email")} />
            {errors.email && <p className="text-sm text-red-600">{errors.email.message}</p>}
          </div>
          <select className="campo" {...register("papel")}>
            <option value="EDITOR">Editor</option>
            <option value="ADMIN">Admin</option>
          </select>
          {convidar.isError && (
            <p className="text-sm text-red-600">
              {convidar.error instanceof Error ? convidar.error.message : "Não foi possível enviar o convite."}
            </p>
          )}
          <button type="submit" disabled={convidar.isPending} className="botao-destaque">
            {convidar.isPending ? "Enviando..." : "Enviar convite"}
          </button>
        </form>
      )}

      <div className="cartao mt-6 overflow-x-auto">
        <table className="w-full border-collapse text-left text-sm">
          <thead>
            <tr className="border-b border-slate-200">
              <th className="py-2">Nome</th>
              <th className="py-2">E-mail</th>
              <th className="py-2">Papel</th>
              <th className="py-2">Status</th>
              <th className="py-2" />
            </tr>
          </thead>
          <tbody>
            {data?.map((usuario) => (
              <tr key={usuario.id} className="border-b border-slate-100 last:border-0">
                <td className="py-2">{usuario.nome}</td>
                <td className="py-2">{usuario.email}</td>
                <td className="py-2">{usuario.papel === "ADMIN" ? "Admin" : "Editor"}</td>
                <td className="py-2"><Status usuario={usuario} /></td>
                <td className="py-2 text-right">
                  <button
                    onClick={() => atualizarAcesso.mutate({ id: usuario.id, ativo: !usuario.ativo })}
                    className="text-sm text-red-600 hover:underline"
                  >
                    {usuario.ativo ? "Desativar" : "Reativar"}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
