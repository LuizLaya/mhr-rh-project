import { afterEach, describe, expect, it, vi } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import LeadsContatoPage from "./LeadsContatoPage";
import { apiFetch } from "../lib/api";

vi.mock("../lib/api", () => ({
  apiFetch: vi.fn(),
}));

const LEAD = {
  id: "lead-1",
  nome: "Luiz Laya",
  email: "junior@exemplo.com",
  status: "CONCLUIDO",
  concluidoEm: null,
  virouContrato: false,
  clienteId: null,
  criadoEm: "2026-01-01T00:00:00Z",
};

const CLIENTE = { id: "cliente-1", nomeEmpresa: "Empresa Teste LTDA", criadoEm: "" };

function renderPagina() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={queryClient}>
      <LeadsContatoPage />
    </QueryClientProvider>,
  );
}

describe("LeadsContatoPage - virar contrato", () => {
  afterEach(() => {
    vi.resetAllMocks();
  });

  it("o botão Confirmar fica desabilitado até um cliente ser selecionado", async () => {
    const user = userEvent.setup();
    vi.mocked(apiFetch).mockImplementation(async (path: string) => {
      if (path === "/api/admin/leads-contato") return [LEAD] as never;
      if (path === "/api/admin/clientes") return [CLIENTE] as never;
      throw new Error(`chamada inesperada: ${path}`);
    });
    renderPagina();

    await user.click(await screen.findByText(/marcar como contrato/i));

    expect(screen.getByRole("button", { name: /confirmar/i })).toBeDisabled();
  });

  it("o select de cliente aparece só depois de clicar em 'Marcar como contrato'", async () => {
    const user = userEvent.setup();
    vi.mocked(apiFetch).mockImplementation(async (path: string) => {
      if (path === "/api/admin/leads-contato") return [LEAD] as never;
      if (path === "/api/admin/clientes") return [CLIENTE] as never;
      throw new Error(`chamada inesperada: ${path}`);
    });
    renderPagina();

    expect(await screen.findAllByRole("combobox")).toHaveLength(1);

    await user.click(await screen.findByText(/marcar como contrato/i));

    expect(screen.getAllByRole("combobox")).toHaveLength(2);
  });

  it("ao selecionar um cliente e confirmar, chama o PATCH com virouContrato=true e o clienteId", async () => {
    const user = userEvent.setup();
    const chamadaPatch = vi.fn().mockResolvedValue({ ...LEAD, virouContrato: true, clienteId: CLIENTE.id });
    vi.mocked(apiFetch).mockImplementation(async (path: string, init?: RequestInit) => {
      if (path === "/api/admin/leads-contato") return [LEAD] as never;
      if (path === "/api/admin/clientes") return [CLIENTE] as never;
      if (path === `/api/admin/leads-contato/${LEAD.id}/cliente`) return chamadaPatch(path, init) as never;
      throw new Error(`chamada inesperada: ${path}`);
    });
    renderPagina();

    await user.click(await screen.findByText(/marcar como contrato/i));
    const comboboxCliente = screen.getAllByRole("combobox")[1];
    await user.selectOptions(comboboxCliente, CLIENTE.id);
    await user.click(screen.getByRole("button", { name: /confirmar/i }));

    await waitFor(() => {
      expect(chamadaPatch).toHaveBeenCalledWith(
        `/api/admin/leads-contato/${LEAD.id}/cliente`,
        expect.objectContaining({
          method: "PATCH",
          body: JSON.stringify({ virouContrato: true, clienteId: CLIENTE.id }),
        }),
      );
    });
  });
});
