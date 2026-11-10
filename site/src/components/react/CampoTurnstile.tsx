import { useEffect, useRef } from "react";

const SITE_KEY = import.meta.env.PUBLIC_TURNSTILE_SITE_KEY;

interface CampoTurnstileProps {
  aoValidar: (token: string) => void;
}

interface ApiTurnstile {
  render(container: HTMLElement, options: { sitekey: string; callback: (token: string) => void }): string;
  remove(widgetId: string): void;
}

/**
 * Widget do Cloudflare Turnstile (anti-spam) usado nos formulários públicos de
 * contato e candidatura. Renderização IMPERATIVA (turnstile.render), feita num
 * useEffect, de propósito: o script do Turnstile também escaneia o DOM em busca de
 * elementos ".cf-turnstile" e os renderiza sozinho assim que acha um (modo
 * implícito) - isso mutava o próprio container antes do React terminar de
 * hidratar a ilha (SSR manda um <div> vazio, o Turnstile enche ele de conteúdo
 * no meio do caminho), e o React detectava a divergência e descartava a árvore
 * inteira (erro de hidratação), deixando o botão de enviar travado mesmo com o
 * desafio resolvido. Sem a classe/atributos que o modo implícito procura, só a
 * nossa própria chamada (depois da hidratação) preenche o container.
 */
export default function CampoTurnstile({ aoValidar }: CampoTurnstileProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const widgetIdRef = useRef<string | null>(null);

  useEffect(() => {
    let cancelado = false;
    let tentativaId: ReturnType<typeof setTimeout>;

    function renderizar() {
      if (cancelado || !containerRef.current || widgetIdRef.current) return;
      const turnstile = (window as unknown as { turnstile?: ApiTurnstile }).turnstile;
      if (!turnstile) {
        tentativaId = setTimeout(renderizar, 100);
        return;
      }
      widgetIdRef.current = turnstile.render(containerRef.current, {
        sitekey: SITE_KEY,
        callback: aoValidar,
      });
    }
    renderizar();

    return () => {
      cancelado = true;
      clearTimeout(tentativaId);
      const turnstile = (window as unknown as { turnstile?: ApiTurnstile }).turnstile;
      if (turnstile && widgetIdRef.current) {
        turnstile.remove(widgetIdRef.current);
      }
      widgetIdRef.current = null;
    };
  }, [aoValidar]);

  return <div ref={containerRef} />;
}
