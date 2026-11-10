import { useEffect, type ReactNode } from "react";
import { X } from "lucide-react";

interface ModalProps {
  titulo: string;
  aberto: boolean;
  aoFechar: () => void;
  children: ReactNode;
}

/**
 * Modal simples (sem biblioteca externa de dialog): fundo escurecido + caixa
 * centralizada. Fecha clicando fora, no X, ou com Esc — usado hoje só para ver
 * a mensagem completa de um log, mas é genérico o bastante para outros casos.
 */
export default function Modal({ titulo, aberto, aoFechar, children }: ModalProps) {
  useEffect(() => {
    if (!aberto) return;
    function aoApertarTecla(e: KeyboardEvent) {
      if (e.key === "Escape") aoFechar();
    }
    window.addEventListener("keydown", aoApertarTecla);
    return () => window.removeEventListener("keydown", aoApertarTecla);
  }, [aberto, aoFechar]);

  if (!aberto) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4"
      onClick={aoFechar}
    >
      <div
        role="dialog"
        aria-modal="true"
        aria-label={titulo}
        className="max-h-[80vh] w-full max-w-lg overflow-y-auto rounded-lg bg-white p-5 shadow-lg"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-start justify-between gap-4">
          <h2 className="text-lg font-semibold text-mhr-navy">{titulo}</h2>
          <button onClick={aoFechar} aria-label="Fechar" className="text-slate-400 hover:text-slate-600">
            <X size={20} />
          </button>
        </div>
        <div className="mt-3 text-sm text-slate-700">{children}</div>
      </div>
    </div>
  );
}
