import { useState, type ChangeEvent } from "react";
import { Upload } from "lucide-react";
import { apiFetch } from "../lib/api";

interface CampoImagemProps {
  valorAtual?: string;
  aoEnviar: (url: string) => void;
}

/**
 * Botão de upload de imagem (POST /api/admin/uploads) que preenche o campo de URL
 * já existente no formulário - a URL continua editável manualmente também, pra
 * quem preferir colar um link já hospedado em outro lugar (ex.: imagem que já
 * está em outro site).
 */
export default function CampoImagem({ valorAtual, aoEnviar }: CampoImagemProps) {
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  async function selecionarArquivo(e: ChangeEvent<HTMLInputElement>) {
    const arquivo = e.target.files?.[0];
    e.target.value = ""; // permite escolher o mesmo arquivo de novo depois, se precisar
    if (!arquivo) return;

    setErro(null);
    setEnviando(true);
    try {
      const formData = new FormData();
      formData.append("arquivo", arquivo);
      const resultado = await apiFetch<{ url: string }>("/api/admin/uploads", { method: "POST", body: formData });
      aoEnviar(resultado.url);
    } catch (erroEnvio) {
      setErro(erroEnvio instanceof Error ? erroEnvio.message : "Não foi possível enviar a imagem.");
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="flex items-center gap-3">
      {valorAtual && (
        <img src={valorAtual} alt="" className="h-12 w-12 rounded border border-slate-200 object-cover" />
      )}
      <label className="botao-link inline-flex cursor-pointer items-center gap-1 text-sm">
        <Upload size={14} />
        {enviando ? "Enviando..." : "Enviar imagem"}
        <input
          type="file"
          accept="image/jpeg,image/png,image/webp,image/gif"
          className="hidden"
          disabled={enviando}
          onChange={selecionarArquivo}
        />
      </label>
      {erro && <span className="text-sm text-red-600">{erro}</span>}
    </div>
  );
}
