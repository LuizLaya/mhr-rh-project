import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { instalarMensagensZodPtBr } from "../../lib/zod-pt-br";
import { mascararTelefone } from "../../lib/mascaras";
import CampoTurnstile from "./CampoTurnstile";

instalarMensagensZodPtBr();

const candidaturaSchema = z.object({
  nomeCandidato: z.string().min(2, "Informe seu nome"),
  emailCandidato: z.string().email("E-mail inválido"),
  telefoneCandidato: z.string().optional(),
  urlCurriculo: z.string().url("Informe um link válido (ex.: Google Drive, LinkedIn)"),
  consentimentoLgpd: z.boolean().refine((v) => v, "É preciso aceitar o uso dos dados pessoais (LGPD)"),
});

type CandidaturaFormValues = z.infer<typeof candidaturaSchema>;

const API_BASE_URL = import.meta.env.PUBLIC_API_BASE_URL;

interface CandidaturaFormProps {
  vagaId: string;
}

/**
 * Formulário público de candidatura a uma vaga. O currículo é um link já hospedado
 * pelo próprio candidato (Google Drive, LinkedIn etc.) - upload direto de arquivo
 * fica para a Fase 3 (ver TODO em CandidaturaController no backend).
 */
export default function CandidaturaForm({ vagaId }: CandidaturaFormProps) {
  const [status, setStatus] = useState<"idle" | "sending" | "sent" | "error">("idle");
  const [turnstileToken, setTurnstileToken] = useState("");
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<CandidaturaFormValues>({ resolver: zodResolver(candidaturaSchema) });

  const { onChange: aoMudarTelefone, ...registroTelefone } = register("telefoneCandidato");

  async function onSubmit(values: CandidaturaFormValues) {
    if (!turnstileToken) {
      setStatus("error");
      return;
    }
    setStatus("sending");
    try {
      const res = await fetch(`${API_BASE_URL}/api/vagas/${vagaId}/candidaturas`, {
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

  if (status === "sent") {
    return (
      <p className="rounded border border-green-200 bg-green-50 p-4 text-sm text-green-700">
        Candidatura enviada! Em breve entraremos em contato.
      </p>
    );
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
      <div>
        <label className="text-sm font-medium" htmlFor="nomeCandidato">Nome</label>
        <input id="nomeCandidato" className="campo mt-1" {...register("nomeCandidato")} />
        {errors.nomeCandidato && <p className="mt-1 text-sm text-red-600">{errors.nomeCandidato.message}</p>}
      </div>
      <div>
        <label className="text-sm font-medium" htmlFor="emailCandidato">E-mail</label>
        <input id="emailCandidato" type="email" className="campo mt-1" {...register("emailCandidato")} />
        {errors.emailCandidato && <p className="mt-1 text-sm text-red-600">{errors.emailCandidato.message}</p>}
      </div>
      <div>
        <label className="text-sm font-medium" htmlFor="telefoneCandidato">Telefone <span className="font-normal text-slate-400">(opcional)</span></label>
        <input
          id="telefoneCandidato"
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
        <label className="text-sm font-medium" htmlFor="urlCurriculo">Link do currículo</label>
        <input
          id="urlCurriculo"
          type="url"
          placeholder="https://..."
          className="campo mt-1"
          {...register("urlCurriculo")}
        />
        <p className="mt-1 text-xs text-slate-400">Google Drive, LinkedIn ou qualquer link público do seu currículo.</p>
        {errors.urlCurriculo && <p className="mt-1 text-sm text-red-600">{errors.urlCurriculo.message}</p>}
      </div>
      <div>
        <label className="flex items-start gap-2 text-sm text-slate-600">
          <input type="checkbox" className="mt-1" {...register("consentimentoLgpd")} />
          Autorizo o uso dos meus dados pessoais para fins de recrutamento e seleção, conforme a LGPD.
        </label>
        {errors.consentimentoLgpd && <p className="mt-1 text-sm text-red-600">{errors.consentimentoLgpd.message}</p>}
      </div>

      <CampoTurnstile aoValidar={setTurnstileToken} />

      <button type="submit" disabled={status === "sending" || !turnstileToken} className="botao-primario w-full">
        {status === "sending" ? "Enviando..." : "Enviar candidatura"}
      </button>

      {status === "error" && <p className="text-sm text-red-600">Não foi possível enviar agora. Tente novamente.</p>}
    </form>
  );
}
