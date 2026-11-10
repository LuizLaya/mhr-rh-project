import { useState } from "react";
import { Link } from "react-router";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";

const schema = z.object({
  email: z.string().email("E-mail inválido"),
});

type FormValues = z.infer<typeof schema>;

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

/**
 * Pede o e-mail e dispara POST /api/auth/esqueci-senha. Sempre mostra a mesma
 * mensagem de sucesso (ver comentário abaixo) - o link de verdade chega por e-mail,
 * essa tela não sabe (nem pode saber) se o envio funcionou.
 */
export default function EsqueciSenhaPage() {
  const [enviado, setEnviado] = useState(false);
  const [enviando, setEnviando] = useState(false);
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<FormValues>({ resolver: zodResolver(schema) });

  async function onSubmit(values: FormValues) {
    setEnviando(true);
    try {
      await fetch(`${API_BASE_URL}/api/auth/esqueci-senha`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(values),
      });
    } finally {
      // Sempre mostra a mesma mensagem, exista ou nao o e-mail: evita confirmar
      // para quem esta testando quais e-mails tem cadastro no painel.
      setEnviado(true);
      setEnviando(false);
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
      <div className="w-full max-w-sm rounded-lg border border-slate-200 bg-white p-8 shadow-sm">
        <img src="/logo.png" alt="MHR" className="mx-auto mb-6 h-10 w-auto" />
        <h1 className="mb-2 text-center text-xl font-bold text-mhr-navy">Esqueci minha senha</h1>

        {enviado ? (
          <p className="mt-4 text-center text-sm text-slate-600">
            Se esse e-mail tiver cadastro no painel, enviamos um link para redefinir a
            senha. Confira sua caixa de entrada (e o spam).
          </p>
        ) : (
          <form onSubmit={handleSubmit(onSubmit)}>
            <p className="mb-4 text-center text-sm text-slate-600">
              Informe seu e-mail para receber um link de redefinição.
            </p>
            <label className="text-sm font-medium" htmlFor="email">E-mail</label>
            <input id="email" type="email" className="campo mt-1 mb-3" {...register("email")} />
            {errors.email && <p className="mb-2 text-sm text-red-600">{errors.email.message}</p>}

            <button type="submit" disabled={enviando} className="botao-primario w-full">
              {enviando ? "Enviando..." : "Enviar link"}
            </button>
          </form>
        )}

        <Link to="/login" className="botao-link mt-6 block text-center">Voltar para o login</Link>
      </div>
    </div>
  );
}
