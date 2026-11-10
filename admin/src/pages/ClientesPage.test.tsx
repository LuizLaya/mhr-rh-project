import { afterEach, describe, expect, it, vi } from "vitest";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import ClientesPage from "./ClientesPage";
import { apiFetch } from "../lib/api";

vi.mock("../lib/api", () => ({
  apiFetch: vi.fn(),
}));

const PAGINA_VAZIA = { content: [], totalElements: 0, totalPages: 0, number: 0 };

const CLIENTE = {
  id: "cliente-1",
  nomeEmpresa: "Empresa Teste LTDA",
  exibirNoSite: false,
  criadoEm: "2026-01-01T00:00:00Z",
};

function renderPagina() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={queryClient}>
      <ClientesPage />
    </QueryClientProvider>,
  );
}

describe("ClientesPage - busca e paginação", () => {
  afterEach(() => {
    vi.resetAllMocks();
  });

  it("ao digitar na busca, refaz a chamada com o termo e volta pra página 0", async () => {
    vi.mocked(apiFetch).mockImplementation(async (path: string) => {
      if (path.startsWith("/api/admin/clientes/paginado")) {
        if (path.includes("busca=teste")) {
          return { content: [CLIENTE], totalElements: 1, totalPages: 1, number: 0 } as never;
        }
        return PAGINA_VAZIA as never;
      }
      throw new Error(`chamada inesperada: ${path}`);
    });
    renderPagina();
    await screen.findByText("Nenhum cliente cadastrado ainda.");

    fireEvent.change(screen.getByPlaceholderText("Buscar por empresa ou contato..."), { target: { value: "teste" } });

    await waitFor(() => {
      expect(apiFetch).toHaveBeenCalledWith("/api/admin/clientes/paginado?busca=teste&page=0&size=20");
    });
    expect(await screen.findByText("Empresa Teste LTDA")).toBeInTheDocument();
  });

  it("com mais de uma página, mostra os controles de navegação com o total de clientes", async () => {
    vi.mocked(apiFetch).mockResolvedValue({
      content: [CLIENTE],
      totalElements: 25,
      totalPages: 2,
      number: 0,
    } as never);
    renderPagina();

    expect(await screen.findByText(/Página 1 de 2/)).toBeInTheDocument();
    expect(screen.getByText(/25 clientes/)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /anterior/i })).toBeDisabled();
    expect(screen.getByRole("button", { name: /próxima/i })).toBeEnabled();
  });
});
