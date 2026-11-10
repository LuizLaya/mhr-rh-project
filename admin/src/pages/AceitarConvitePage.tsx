import { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import CampoSenha from "../components/CampoSenha";

const schema = z
  .object({
    novaSenha: z.string().min(8, "A senha deve ter pelo menos 8 caracteres"),
    confirmarSenha: z.string().min(1, "Confirme a senha"),
  })
  .refine((dados) => dados.novaSenha === dados.confirmarSenha, {
    message: "As senhas não coincidem",
    path: ["confirmarSenha"],
  });

type FormValues = z.infer<typeof schema>;

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

/**
 * Tela que fecha o convite enviado por um ADMIN (ver ConviteUsuarioService no backend):
 * a pessoa convidada define a própria senha aqui e já pode logar em seguida. Usa o
 * mesmo endpoint de "esqueci minha senha" — para o backend as duas operações são
 * idênticas: "com este token, defina uma senha para este usuário".
 */
export default function AceitarConvitePage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<FormValues>({ resolver: zodResolver(schema) });

  async function onSubmit(values: FormValues) {
    if (!token) return;
    setErro(null);
    setEnviando(true);
    try {
      const res = await fetch(`${API_BASE_URL}/api/auth/redefinir-senha`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ token, novaSenha: values.novaSenha }),
      });

      if (!res.ok) {
        const corpo = await res.json().catch(() => null);
        setErro(corpo?.message ?? "Não foi possível ativar o acesso.");
        return;
      }

      navigate("/login");
    } catch {
      setErro("Não foi possível conectar à API.");
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
      <div className="w-full max-w-sm rounded-lg border border-slate-200 bg-white p-8 shadow-sm">
        <img src="/logo.png" alt="MHR" className="mx-auto mb-6 h-10 w-auto" />
        <h1 className="mb-2 text-center text-xl font-bold text-mhr-navy">Bem-vindo(a) à MHR</h1>

        {!token ? (
          <p className="text-center text-sm text-red-600">
            Link de convite inválido. Peça para quem te convidou enviar um novo.
          </p>
        ) : (
          <form onSubmit={handleSubmit(onSubmit)}>
            <p className="mb-4 text-center text-sm text-slate-600">
              Defina uma senha para ativar seu acesso ao painel.
            </p>
            <CampoSenha id="novaSenha" label="Senha" registro={register("novaSenha")} erro={errors.novaSenha?.message} className="mb-3" />
            <CampoSenha
              id="confirmarSenha"
              label="Confirme a senha"
              registro={register("confirmarSenha")}
              erro={errors.confirmarSenha?.message}
              className="mb-3"
            />

            {erro && <p className="mb-3 text-sm text-red-600">{erro}</p>}

            <button type="submit" disabled={enviando} className="botao-primario w-full">
              {enviando ? "Ativando..." : "Ativar acesso"}
            </button>
          </form>
        )}

        <Link to="/login" className="botao-link mt-6 block text-center">Já tenho acesso — ir para o login</Link>
      </div>
    </div>
  );
}
