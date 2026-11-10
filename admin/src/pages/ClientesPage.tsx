import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { apiFetch } from "../lib/api";
import { mascararCnpj, mascararTelefone } from "../lib/mascaras";
import CampoImagem from "../components/CampoImagem";

/** Espelha o ClienteDto do backend (br.com.mhr.backend.cliente). */
export interface Cliente {
  id: string;
  nomeEmpresa: string;
  cnpj?: string;
  contatoNome?: string;
  contatoEmail?: string;
  contatoTelefone?: string;
  observacoes?: string;
  logoUrl?: string;
  exibirNoSite: boolean;
  criadoEm: string;
}

const clienteSchema = z.object({
  nomeEmpresa: z.string().min(2),
  cnpj: z.string().optional(),
  contatoNome: z.string().optional(),
  contatoEmail: z.string().email("E-mail inválido").optional().or(z.literal("")),
  contatoTelefone: z.string().optional(),
  observacoes: z.string().optional(),
  logoUrl: z.string().optional(),
  exibirNoSite: z.boolean(),
});

type ClienteFormValues = z.infer<typeof clienteSchema>;

/** Espelha o shape padrão de Page<T> do Spring Data (só os campos usados aqui). */
interface Pagina<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
}

/** Empresas com contrato fechado com a MHR. Depoimentos e mensagens "viradas em contrato" podem apontar para cá. */
export default function ClientesPage() {
  const queryClient = useQueryClient();
  const [mostrarForm, setMostrarForm] = useState(false);
  const [editando, setEditando] = useState<string | null>(null);
  const [busca, setBusca] = useState("");
  const [pagina, setPagina] = useState(0);

  const { data, isLoading, error } = useQuery({
    queryKey: ["admin", "clientes", "paginado", busca, pagina],
    queryFn: () =>
      apiFetch<Pagina<Cliente>>(
        `/api/admin/clientes/paginado?busca=${encodeURIComponent(busca)}&page=${pagina}&size=20`
      ),
  });

  const {
    register,
    handleSubmit,
    reset,
    watch,
    setValue,
    formState: { errors },
  } = useForm<ClienteFormValues>({ resolver: zodResolver(clienteSchema), defaultValues: { exibirNoSite: false } });

  const { onChange: aoMudarCnpj, ...registroCnpj } = register("cnpj");
  const { onChange: aoMudarTelefone, ...registroTelefone } = register("contatoTelefone");

  function fecharForm() {
    reset({
      nomeEmpresa: "",
      cnpj: "",
      contatoNome: "",
      contatoEmail: "",
      contatoTelefone: "",
      observacoes: "",
      logoUrl: "",
      exibirNoSite: false,
    });
    setEditando(null);
    setMostrarForm(false);
  }

  function editar(cliente: Cliente) {
    reset(cliente);
    setEditando(cliente.id);
    setMostrarForm(true);
  }

  const criarCliente = useMutation({
    mutationFn: (values: ClienteFormValues) =>
      apiFetch<Cliente>("/api/admin/clientes", { method: "POST", body: JSON.stringify(values) }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "clientes"] });
      fecharForm();
    },
  });

  const atualizarCliente = useMutation({
    mutationFn: (values: ClienteFormValues) =>
      apiFetch<Cliente>(`/api/admin/clientes/${editando}`, { method: "PUT", body: JSON.stringify(values) }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "clientes"] });
      fecharForm();
    },
  });

  const salvando = criarCliente.isPending || atualizarCliente.isPending;
  const erroAoSalvar = criarCliente.isError || atualizarCliente.isError;

  const excluirCliente = useMutation({
    mutationFn: (id: string) => apiFetch<void>(`/api/admin/clientes/${id}`, { method: "DELETE" }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["admin", "clientes"] }),
  });

  if (isLoading) return <p>Carregando clientes...</p>;
  if (error) return <p className="text-red-600">Erro ao carregar clientes.</p>;

  const clientes = data?.content ?? [];

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-2xl font-bold text-mhr-navy">Clientes</h1>
        <div className="flex items-center gap-3">
          <input
            value={busca}
            onChange={(e) => {
              setBusca(e.target.value);
              setPagina(0);
            }}
            placeholder="Buscar por empresa ou contato..."
            className="campo w-56"
          />
          <button onClick={() => (mostrarForm ? fecharForm() : setMostrarForm(true))} className="botao-primario">
            {mostrarForm ? "Cancelar" : "Novo cliente"}
          </button>
        </div>
      </div>

      {mostrarForm && (
        <form
          onSubmit={handleSubmit((values) => (editando ? atualizarCliente.mutate(values) : criarCliente.mutate(values)))}
          className="cartao mt-4 grid max-w-xl gap-3"
        >
          <div>
            <input placeholder="Nome da empresa" className="campo" {...register("nomeEmpresa")} />
            {errors.nomeEmpresa && <p className="text-sm text-red-600">{errors.nomeEmpresa.message}</p>}
          </div>
          <input
            placeholder="CNPJ (opcional)"
            inputMode="numeric"
            maxLength={18}
            className="campo"
            {...registroCnpj}
            onChange={(e) => {
              e.target.value = mascararCnpj(e.target.value);
              aoMudarCnpj(e);
            }}
          />
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            <input placeholder="Contato (nome)" className="campo" {...register("contatoNome")} />
            <input
              placeholder="Contato (telefone)"
              type="tel"
              maxLength={16}
              className="campo"
              {...registroTelefone}
              onChange={(e) => {
                e.target.value = mascararTelefone(e.target.value);
                aoMudarTelefone(e);
              }}
            />
          </div>
          <div>
            <input placeholder="Contato (e-mail)" type="email" className="campo" {...register("contatoEmail")} />
            {errors.contatoEmail && <p className="text-sm text-red-600">{errors.contatoEmail.message}</p>}
          </div>
          <textarea placeholder="Observações (opcional)" rows={3} className="campo" {...register("observacoes")} />

          <div className="border-t border-slate-100 pt-3">
            <label className="flex items-center gap-2 text-sm">
              <input type="checkbox" {...register("exibirNoSite")} />
              Mostrar este cliente na home do site (nome + logo)
            </label>
            <p className="mt-1 text-xs text-slate-400">
              Só os campos de nome e logo aparecem publicamente — CNPJ, contato e observações nunca saem daqui.
            </p>
            <input placeholder="URL do logo (opcional)" className="campo mt-2" {...register("logoUrl")} />
            <div className="mt-2">
              <CampoImagem valorAtual={watch("logoUrl")} aoEnviar={(url) => setValue("logoUrl", url, { shouldDirty: true })} />
            </div>
          </div>

          {erroAoSalvar && <p className="text-sm text-red-600">Não foi possível salvar o cliente.</p>}
          <button type="submit" disabled={salvando} className="botao-destaque">
            {salvando ? "Salvando..." : editando ? "Salvar alterações" : "Salvar cliente"}
          </button>
        </form>
      )}

      <div className="cartao mt-6 overflow-x-auto">
        {clientes.length === 0 ? (
          <p className="text-sm text-slate-500">
            {busca ? "Nenhum cliente encontrado para essa busca." : "Nenhum cliente cadastrado ainda."}
          </p>
        ) : (
          <table className="w-full border-collapse text-left text-sm">
            <thead>
              <tr className="border-b border-slate-200">
                <th className="py-2">Empresa</th>
                <th className="py-2">Contato</th>
                <th className="py-2">CNPJ</th>
                <th className="py-2">No site</th>
                <th className="py-2" />
              </tr>
            </thead>
            <tbody>
              {clientes.map((cliente) => (
                <tr key={cliente.id} className="border-b border-slate-100 last:border-0">
                  <td className="py-2">{cliente.nomeEmpresa}</td>
                  <td className="py-2">
                    {cliente.contatoNome ?? "—"}
                    {cliente.contatoEmail ? ` · ${cliente.contatoEmail}` : ""}
                  </td>
                  <td className="py-2">{cliente.cnpj ?? "—"}</td>
                  <td className="py-2">
                    <span className={`badge ${cliente.exibirNoSite ? "bg-green-100 text-green-700" : "bg-slate-100 text-slate-600"}`}>
                      {cliente.exibirNoSite ? "Sim" : "Não"}
                    </span>
                  </td>
                  <td className="py-2 text-right">
                    <button onClick={() => editar(cliente)} className="mr-3 text-sm text-mhr-gold hover:underline">
                      Editar
                    </button>
                    <button onClick={() => excluirCliente.mutate(cliente.id)} className="text-sm text-red-600 hover:underline">
                      Excluir
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {data && data.totalPages > 1 && (
        <div className="mt-3 flex items-center justify-between text-sm text-slate-500">
          <button
            onClick={() => setPagina((p) => Math.max(0, p - 1))}
            disabled={pagina === 0}
            className="botao-link disabled:cursor-not-allowed disabled:opacity-40"
          >
            Anterior
          </button>
          <span>
            Página {data.number + 1} de {data.totalPages} ({data.totalElements} clientes)
          </span>
          <button
            onClick={() => setPagina((p) => Math.min(data.totalPages - 1, p + 1))}
            disabled={pagina >= data.totalPages - 1}
            className="botao-link disabled:cursor-not-allowed disabled:opacity-40"
          >
            Próxima
          </button>
        </div>
      )}
    </div>
  );
}
