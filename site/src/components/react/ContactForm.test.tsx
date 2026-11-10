import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { act, fireEvent, render, screen, waitFor } from "@testing-library/react";
import ContactForm from "./ContactForm";

type CallbackTurnstile = (token: string) => void;

function instalarTurnstileFalso() {
  let callback: CallbackTurnstile = () => {};
  vi.stubGlobal("turnstile", {
    render: (_el: HTMLElement, opcoes: { callback: CallbackTurnstile }) => {
      callback = opcoes.callback;
      return "widget-1";
    },
    remove: vi.fn(),
  });
  // act() porque essa chamada simula o script da Cloudflare invocando o callback
  // diretamente, fora de qualquer evento do DOM que o Testing Library já envolveria
  // sozinho - sem isso, o setState (setTurnstileToken) fica pendente e as asserções
  // seguintes ainda veem o estado antigo.
  return {
    resolverDesafio: async (token = "token-de-teste") => {
      await act(async () => {
        callback(token);
      });
    },
  };
}

describe("ContactForm", () => {
  beforeEach(() => {
    vi.stubGlobal("fetch", vi.fn());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("botão de enviar fica desabilitado até o Turnstile validar", () => {
    instalarTurnstileFalso();
    render(<ContactForm />);

    expect(screen.getByRole("button", { name: /enviar mensagem/i })).toBeDisabled();
  });

  it("habilita o botão de enviar assim que o Turnstile resolve o desafio", async () => {
    const { resolverDesafio } = instalarTurnstileFalso();
    render(<ContactForm />);

    await resolverDesafio();

    expect(screen.getByRole("button", { name: /enviar mensagem/i })).toBeEnabled();
  });

  it("com campos obrigatorios em branco, mostra erro de validação sem chamar fetch", async () => {
    const { resolverDesafio } = instalarTurnstileFalso();
    render(<ContactForm />);
    await resolverDesafio();

    fireEvent.click(screen.getByRole("button", { name: /enviar mensagem/i }));

    expect(await screen.findByText("Informe seu nome")).toBeInTheDocument();
    expect(fetch).not.toHaveBeenCalled();
  });

  it("com os campos preenchidos, envia o token do turnstile junto e mostra sucesso", async () => {
    vi.mocked(fetch).mockResolvedValue(new Response(null, { status: 201 }));
    const { resolverDesafio } = instalarTurnstileFalso();
    render(<ContactForm />);
    await resolverDesafio("token-abc");

    fireEvent.change(screen.getByLabelText("Nome"), { target: { value: "Fulano" } });
    fireEvent.change(screen.getByLabelText("E-mail"), { target: { value: "fulano@example.com" } });
    fireEvent.click(screen.getByRole("button", { name: /enviar mensagem/i }));

    expect(await screen.findByText(/mensagem enviada/i)).toBeInTheDocument();
    const [, init] = vi.mocked(fetch).mock.calls[0];
    const corpo = JSON.parse(init?.body as string);
    expect(corpo.turnstileToken).toBe("token-abc");
    expect(corpo.nome).toBe("Fulano");
  });

  it("quando o backend recusa, mostra mensagem de erro", async () => {
    vi.mocked(fetch).mockResolvedValue(new Response(null, { status: 400 }));
    const { resolverDesafio } = instalarTurnstileFalso();
    render(<ContactForm />);
    await resolverDesafio();

    fireEvent.change(screen.getByLabelText("Nome"), { target: { value: "Fulano" } });
    fireEvent.change(screen.getByLabelText("E-mail"), { target: { value: "fulano@example.com" } });
    fireEvent.click(screen.getByRole("button", { name: /enviar mensagem/i }));

    await waitFor(() => {
      expect(screen.getByText(/não foi possível enviar agora/i)).toBeInTheDocument();
    });
  });
});
