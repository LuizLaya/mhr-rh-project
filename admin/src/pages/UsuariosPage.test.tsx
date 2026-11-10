import { afterEach, describe, expect, it, vi } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router";
import UsuariosPage from "./UsuariosPage";
import { apiFetch } from "../lib/api";

vi.mock("../lib/api", () => ({
  apiFetch: vi.fn(),
}));

const USUARIOS_BASE = [
  { id: "1", nome: "Admin", email: "admin@mhr.com.br", papel: "ADMIN", ativo: true, senhaDefinida: true, criadoEm: "" },
];

function renderPagina() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <UsuariosPage />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe("UsuariosPage - feedback do convite por e-mail", () => {
  afterEach(() => {
    vi.resetAllMocks();
  });

  it("quando o backend confirma emailEnviado=true, mostra a mensagem de sucesso", async () => {
    const user = userEvent.setup();
    vi.mocked(apiFetch).mockImplementation(async (path: string) => {
      if (path === "/api/admin/usuarios") return USUARIOS_BASE as never;
      if (path === "/api/admin/usuarios/convidar") {
        return { usuario: { ...USUARIOS_BASE[0], id: "2", email: "novo@mhr.com.br" }, emailEnviado: true } as never;
      }
      throw new Error(`chamada inesperada: ${path}`);
    });
    renderPagina();

    await user.click(await screen.findByRole("button", { name: /convidar usuário/i }));
    await user.type(screen.getByPlaceholderText("Nome"), "Novo Usuario");
    await user.type(screen.getByPlaceholderText("E-mail"), "novo@mhr.com.br");
    await user.click(screen.getByRole("button", { name: /enviar convite/i }));

    await waitFor(() => {
      expect(screen.getByText(/convite enviado por e-mail/i)).toBeInTheDocument();
    });
    expect(screen.queryByText(/não pôde ser enviado/i)).not.toBeInTheDocument();
  });

  it("quando o backend confirma emailEnviado=false, avisa que precisa repassar o link manualmente", async () => {
    const user = userEvent.setup();
    vi.mocked(apiFetch).mockImplementation(async (path: string) => {
      if (path === "/api/admin/usuarios") return USUARIOS_BASE as never;
      if (path === "/api/admin/usuarios/convidar") {
        return { usuario: { ...USUARIOS_BASE[0], id: "2", email: "fora-do-sandbox@exemplo.com" }, emailEnviado: false } as never;
      }
      throw new Error(`chamada inesperada: ${path}`);
    });
    renderPagina();

    await user.click(await screen.findByRole("button", { name: /convidar usuário/i }));
    await user.type(screen.getByPlaceholderText("Nome"), "Novo Usuario");
    await user.type(screen.getByPlaceholderText("E-mail"), "fora-do-sandbox@exemplo.com");
    await user.click(screen.getByRole("button", { name: /enviar convite/i }));

    await waitFor(() => {
      expect(screen.getByText(/não pôde ser enviado/i)).toBeInTheDocument();
    });
    expect(screen.getByText(/fora-do-sandbox@exemplo\.com/)).toBeInTheDocument();
  });
});
