import { useState } from "react";
import { Eye, EyeOff } from "lucide-react";
import type { UseFormRegisterReturn } from "react-hook-form";

interface CampoSenhaProps {
  id: string;
  label: string;
  registro: UseFormRegisterReturn;
  erro?: string;
  className?: string;
}

/**
 * Campo de senha com um botão de olho que alterna entre ocultar e mostrar o texto
 * digitado (type="password"/"text") - usado em toda tela que pede senha (login,
 * redefinição, aceite de convite), pra não duplicar o botão em cada uma.
 */
export default function CampoSenha({ id, label, registro, erro, className }: CampoSenhaProps) {
  const [visivel, setVisivel] = useState(false);

  return (
    <div className={className}>
      <label className="text-sm font-medium" htmlFor={id}>{label}</label>
      <div className="relative mt-1">
        <input id={id} type={visivel ? "text" : "password"} className="campo pr-10" {...registro} />
        <button
          type="button"
          onClick={() => setVisivel((v) => !v)}
          aria-label={visivel ? "Ocultar senha" : "Mostrar senha"}
          className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
        >
          {visivel ? <EyeOff size={18} /> : <Eye size={18} />}
        </button>
      </div>
      {erro && <p className="mt-1 text-sm text-red-600">{erro}</p>}
    </div>
  );
}
