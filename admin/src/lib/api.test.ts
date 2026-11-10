import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { apiFetch } from "./api";
import { limparToken, salvarSessao } from "./auth";

describe("apiFetch", () => {
  beforeEach(() => {
    localStorage.clear();
    vi.stubGlobal("fetch", vi.fn());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("injeta o Authorization Bearer quando ha um token salvo", async () => {
    salvarSessao("jwt-abc", "Nome", "ADMIN");
    vi.mocked(fetch).mockResolvedValue(
      new Response(JSON.stringify({ ok: true }), { status: 200, headers: { "Content-Type": "application/json" } }),
    );

    await apiFetch("/api/admin/usuarios");

    const [, init] = vi.mocked(fetch).mock.calls[0];
    const headers = init?.headers as Record<string, string>;
    expect(headers.Authorization).toBe("Bearer jwt-abc");
  });

  it("nao envia Authorization quando nao ha sessao", async () => {
    limparToken();
    vi.mocked(fetch).mockResolvedValue(new Response("{}", { status: 200 }));

    await apiFetch("/api/admin/usuarios");

    const [, init] = vi.mocked(fetch).mock.calls[0];
    const headers = init?.headers as Record<string, string>;
    expect(headers.Authorization).toBeUndefined();
  });

  it("em erro, lanca Error com a mensagem devolvida pelo backend", async () => {
    vi.mocked(fetch).mockResolvedValue(
      new Response(JSON.stringify({ message: "Ja existe um usuario cadastrado com esse e-mail" }), { status: 400 }),
    );

    await expect(apiFetch("/api/admin/usuarios/convidar")).rejects.toThrow(
      "Ja existe um usuario cadastrado com esse e-mail",
    );
  });

  it("em erro sem corpo JSON, lanca Error generico com o status", async () => {
    vi.mocked(fetch).mockResolvedValue(new Response("not json", { status: 500 }));

    await expect(apiFetch("/api/admin/usuarios")).rejects.toThrow("Erro na API: 500");
  });

  it("resposta 204 devolve undefined em vez de tentar fazer parse do corpo", async () => {
    vi.mocked(fetch).mockResolvedValue(new Response(null, { status: 204 }));

    const resultado = await apiFetch("/api/admin/depoimentos/algum-id");

    expect(resultado).toBeUndefined();
  });

  it("com body FormData, nao forca Content-Type application/json (deixa o navegador definir o boundary)", async () => {
    vi.mocked(fetch).mockResolvedValue(new Response(JSON.stringify({ url: "/uploads/x.png" }), { status: 201 }));

    await apiFetch("/api/admin/uploads", { method: "POST", body: new FormData() });

    const [, init] = vi.mocked(fetch).mock.calls[0];
    const headers = init?.headers as Record<string, string>;
    expect(headers["Content-Type"]).toBeUndefined();
  });
});
