import { useState } from "react";
import { useNavigate, Link } from "react-router";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { salvarSessao, type Papel } from "../lib/auth";
import CampoSenha from "../components/CampoSenha";

const loginSchema = z.object({
  email: z.string().email("E-mail inválido"),
  senha: z.string().min(1, "Informe a senha"),
});

type LoginValues = z.infer<typeof loginSchema>;

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

/** Tela de login: POST /api/auth/login devolve o JWT que salvarSessao guarda no localStorage. */
export default function LoginPage() {
  const navigate = useNavigate();
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginValues>({ resolver: zodResolver(loginSchema) });

  async function onSubmit(values: LoginValues) {
    setErro(null);
    setEnviando(true);
    try {
      const res = await fetch(`${API_BASE_URL}/api/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(values),
      });

      if (!res.ok) {
        setErro("E-mail ou senha incorretos.");
        return;
      }

      const data: { token: string; nome: string; papel: Papel } = await res.json();
      salvarSessao(data.token, data.nome, data.papel);
      navigate("/");
    } catch {
      setErro("Não foi possível conectar à API.");
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="flex min-h-screen bg-slate-50">
      <div className="relative hidden flex-1 flex-col justify-between overflow-hidden bg-gradient-to-br from-mhr-navy to-[#0a1b2d] p-12 text-white lg:flex">
        <div className="pointer-events-none absolute -right-24 -top-24 h-96 w-96 rounded-full bg-mhr-gold/10" />
        <div className="pointer-events-none absolute -bottom-32 -left-16 h-80 w-80 rounded-full bg-white/5" />

        <div className="inline-block w-fit rounded-lg bg-white px-4 py-2">
          <img src="/logo.png" alt="MHR" className="h-9 w-auto" />
        </div>

        <div className="relative max-w-md">
          <h2 className="text-3xl font-bold leading-tight">Conectando pessoas e oportunidades.</h2>
          <p className="mt-4 text-sm text-white/70">
            Painel administrativo da MHR Recursos Humanos — gerencie vagas, conteúdo do site e o relacionamento com
            clientes em um só lugar.
          </p>
        </div>

        <p className="relative text-xs text-white/40">MHR Recursos Humanos</p>
      </div>

      <div className="flex flex-1 items-center justify-center px-4">
        <form onSubmit={handleSubmit(onSubmit)} className="w-full max-w-sm">
          <img src="/logo.png" alt="MHR" className="mb-6 h-10 w-auto lg:hidden" />
          <h1 className="mb-1 text-2xl font-bold text-mhr-navy">Bem-vindo de volta</h1>
          <p className="mb-6 text-sm text-slate-500">Entre com suas credenciais para acessar o painel.</p>

          <label className="text-sm font-medium" htmlFor="email">E-mail</label>
          <input id="email" type="email" className="campo mt-1 mb-3" {...register("email")} />
          {errors.email && <p className="mb-2 text-sm text-red-600">{errors.email.message}</p>}

          <CampoSenha id="senha" label="Senha" registro={register("senha")} erro={errors.senha?.message} className="mb-1" />
          <Link to="/esqueci-senha" className="botao-link mb-3 mt-1 inline-block">Esqueci minha senha</Link>

          {erro && <p className="mb-3 rounded-lg bg-red-50 p-3 text-sm text-red-600">{erro}</p>}

          <button type="submit" disabled={enviando} className="botao-primario w-full">
            {enviando ? "Entrando..." : "Entrar"}
          </button>
        </form>
      </div>
    </div>
  );
}
