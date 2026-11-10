import { afterEach, describe, expect, it, vi } from "vitest";
import { render, waitFor } from "@testing-library/react";
import CampoTurnstile from "./CampoTurnstile";

/**
 * Regressão do bug real de hidratação: o widget tinha que ser renderizado de forma
 * imperativa (turnstile.render num useEffect), nunca pela classe/atributos "cf-turnstile"
 * que o script da Cloudflare escaneia sozinho (ver comentário no próprio componente) -
 * esses testes travam esse contrato.
 */
describe("CampoTurnstile", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
  });

  it("nao usa a classe/atributos que o script escaneia sozinho (modo implícito)", () => {
    vi.stubGlobal("turnstile", { render: vi.fn().mockReturnValue("widget-1"), remove: vi.fn() });

    const { container } = render(<CampoTurnstile aoValidar={vi.fn()} />);

    expect(container.querySelector(".cf-turnstile")).toBeNull();
    expect(container.querySelector("[data-sitekey]")).toBeNull();
  });

  it("renderiza o widget via turnstile.render quando o script já está carregado", () => {
    const renderMock = vi.fn().mockReturnValue("widget-1");
    vi.stubGlobal("turnstile", { render: renderMock, remove: vi.fn() });
    const aoValidar = vi.fn();

    const { container } = render(<CampoTurnstile aoValidar={aoValidar} />);

    expect(renderMock).toHaveBeenCalledTimes(1);
    const [elementoPassado, opcoes] = renderMock.mock.calls[0];
    expect(elementoPassado).toBe(container.firstChild);
    expect(opcoes.callback).toBe(aoValidar);
  });

  it("espera o script carregar depois, se turnstile ainda nao existir no momento da montagem", async () => {
    const renderMock = vi.fn().mockReturnValue("widget-1");
    render(<CampoTurnstile aoValidar={vi.fn()} />);
    expect(renderMock).not.toHaveBeenCalled();

    vi.stubGlobal("turnstile", { render: renderMock, remove: vi.fn() });

    await waitFor(() => expect(renderMock).toHaveBeenCalledTimes(1), { timeout: 2000 });
  });

  it("remove o widget ao desmontar", () => {
    const removeMock = vi.fn();
    vi.stubGlobal("turnstile", { render: vi.fn().mockReturnValue("widget-1"), remove: removeMock });

    const { unmount } = render(<CampoTurnstile aoValidar={vi.fn()} />);
    unmount();

    expect(removeMock).toHaveBeenCalledWith("widget-1");
  });
});
