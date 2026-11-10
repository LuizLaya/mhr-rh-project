import { afterEach, describe, expect, it, vi } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import CandidaturasPage from "./CandidaturasPage";
import { apiFetch } from "../lib/api";

vi.mock("../lib/api", () => ({
  apiFetch: vi.fn(),
}));

const VAGA = { id: "vaga-1", titulo: "Analista de RH" };

const CANDIDATURA = {
  id: "candidatura-1",
  vagaId: VAGA.id,
  nomeCandidato: "Maria Teste",
  emailCandidato: "maria@exemplo.com",
  urlCurriculo: "https://exemplo.com/curriculo",
  consentimentoLgpd: true,
  status: "NOVA",
  criadoEm: "2026-01-01T00:00:00Z",
};

function renderPagina() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={queryClient}>
      <CandidaturasPage />
    </QueryClientProvider>,
  );
}

describe("CandidaturasPage - mudar status", () => {
  afterEach(() => {
    vi.resetAllMocks();
  });

  it("ao escolher um novo status no select, chama o PATCH com o id da candidatura e o status escolhido", async () => {
    const user = userEvent.setup();
    const chamadaPatch = vi.fn().mockResolvedValue({ ...CANDIDATURA, status: "EM_ANALISE" });
    vi.mocked(apiFetch).mockImplementation(async (path: string, init?: RequestInit) => {
      if (path === "/api/admin/vagas") return [VAGA] as never;
      if (path === "/api/admin/candidaturas") return [CANDIDATURA] as never;
      if (path === `/api/admin/candidaturas/${CANDIDATURA.id}/status`) return chamadaPatch(path, init) as never;
      throw new Error(`chamada inesperada: ${path}`);
    });
    renderPagina();

    const selectStatus = await screen.findByDisplayValue("Nova");
    await user.selectOptions(selectStatus, "EM_ANALISE");

    await waitFor(() => {
      expect(chamadaPatch).toHaveBeenCalledWith(
        `/api/admin/candidaturas/${CANDIDATURA.id}/status`,
        expect.objectContaining({
          method: "PATCH",
          body: JSON.stringify({ status: "EM_ANALISE" }),
        }),
      );
    });
  });

  it("ao trocar o filtro de vaga, busca as candidaturas só daquela vaga", async () => {
    const user = userEvent.setup();
    vi.mocked(apiFetch).mockImplementation(async (path: string) => {
      if (path === "/api/admin/vagas") return [VAGA] as never;
      if (path === "/api/admin/candidaturas") return [CANDIDATURA] as never;
      if (path === `/api/admin/candidaturas?vagaId=${VAGA.id}`) return [CANDIDATURA] as never;
      throw new Error(`chamada inesperada: ${path}`);
    });
    renderPagina();
    await screen.findByText("Maria Teste");

    const selectVaga = screen.getByDisplayValue("Todas as vagas");
    await user.selectOptions(selectVaga, VAGA.id);

    await waitFor(() => {
      expect(apiFetch).toHaveBeenCalledWith(`/api/admin/candidaturas?vagaId=${VAGA.id}`);
    });
  });
});
