import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { instalarMensagensZodPtBr } from "../../lib/zod-pt-br";
import { mascararTelefone } from "../../lib/mascaras";
import CampoTurnstile from "./CampoTurnstile";

instalarMensagensZodPtBr();

const contactSchema = z.object({
  nome: z.string().min(2, "Informe seu nome"),
  email: z.string().email("E-mail inválido"),
  telefone: z.string().optional(),
  nomeEmpresa: z.string().optional(),
  mensagem: z.string().optional(),
});

type ContactFormValues = z.infer<typeof contactSchema>;

const API_BASE_URL = import.meta.env.PUBLIC_API_BASE_URL;

/** Formulário público de contato (página /contato), posta em POST /api/leads-contato. */
export default function ContactForm() {
  const [status, setStatus] = useState<"idle" | "sending" | "sent" | "error">("idle");
  const [turnstileToken, setTurnstileToken] = useState("");
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<ContactFormValues>({ resolver: zodResolver(contactSchema) });

  const { onChange: aoMudarTelefone, ...registroTelefone } = register("telefone");

  async function onSubmit(values: ContactFormValues) {
    if (!turnstileToken) {
      setStatus("error");
      return;
    }
    setStatus("sending");
    try {
      const res = await fetch(`${API_BASE_URL}/api/leads-contato`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ ...values, turnstileToken }),
      });
      if (!res.ok) throw new Error();
      setStatus("sent");
      reset();
    } catch {
      setStatus("error");
    }
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
      <div>
        <label className="text-sm font-medium" htmlFor="nome">Nome</label>
        <input id="nome" className="campo mt-1" {...register("nome")} />
        {errors.nome && <p className="mt-1 text-sm text-red-600">{errors.nome.message}</p>}
      </div>
      <div>
        <label className="text-sm font-medium" htmlFor="email">E-mail</label>
        <input id="email" type="email" className="campo mt-1" {...register("email")} />
        {errors.email && <p className="mt-1 text-sm text-red-600">{errors.email.message}</p>}
      </div>
      <div>
        <label className="text-sm font-medium" htmlFor="telefone">Telefone</label>
        <input
          id="telefone"
          type="tel"
          placeholder="(61) 90000-0000"
          maxLength={16}
          className="campo mt-1"
          {...registroTelefone}
          onChange={(e) => {
            e.target.value = mascararTelefone(e.target.value);
            aoMudarTelefone(e);
          }}
        />
      </div>
      <div>
        <label className="text-sm font-medium" htmlFor="nomeEmpresa">Empresa</label>
        <input id="nomeEmpresa" className="campo mt-1" {...register("nomeEmpresa")} />
      </div>
      <div>
        <label className="text-sm font-medium" htmlFor="mensagem">Mensagem <span className="font-normal text-slate-400">(opcional)</span></label>
        <textarea id="mensagem" rows={5} className="campo mt-1" {...register("mensagem")} />
      </div>

      <CampoTurnstile aoValidar={setTurnstileToken} />

      <button type="submit" disabled={status === "sending" || !turnstileToken} className="botao-primario w-full">
        {status === "sending" ? "Enviando..." : "Enviar mensagem"}
      </button>

      {status === "sent" && <p className="text-sm text-green-700">Mensagem enviada! Em breve entraremos em contato.</p>}
      {status === "error" && <p className="text-sm text-red-600">Não foi possível enviar agora. Tente novamente.</p>}
    </form>
  );
}
