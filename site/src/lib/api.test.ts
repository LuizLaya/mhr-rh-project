import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { buscarVagas, buscarServicos, buscarFaq } from "./api";

describe("funções de busca da API pública", () => {
  beforeEach(() => {
    vi.stubGlobal("fetch", vi.fn());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("buscarVagas devolve o JSON da resposta quando ok", async () => {
    const vagas = [{ id: "1", titulo: "Dev", slug: "dev" }];
    vi.mocked(fetch).mockResolvedValue(new Response(JSON.stringify(vagas), { status: 200 }));

    await expect(buscarVagas()).resolves.toEqual(vagas);
  });

  it("buscarVagas lanca erro quando a resposta nao e ok", async () => {
    vi.mocked(fetch).mockResolvedValue(new Response("", { status: 500 }));

    await expect(buscarVagas()).rejects.toThrow("Falha ao carregar vagas");
  });

  it("buscarServicos lanca erro com a mensagem certa quando a resposta nao e ok", async () => {
    vi.mocked(fetch).mockResolvedValue(new Response("", { status: 500 }));

    await expect(buscarServicos()).rejects.toThrow("Falha ao carregar servicos");
  });

  it("buscarFaq devolve o JSON da resposta quando ok", async () => {
    const faq = [{ id: "1", pergunta: "P?", resposta: "R.", ordemExibicao: 0 }];
    vi.mocked(fetch).mockResolvedValue(new Response(JSON.stringify(faq), { status: 200 }));

    await expect(buscarFaq()).resolves.toEqual(faq);
  });
});
