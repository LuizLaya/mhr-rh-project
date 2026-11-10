import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router";
import RotaProtegida from "./RotaProtegida";
import { limparToken, salvarSessao } from "../lib/auth";

function renderComRota() {
  return render(
    <MemoryRouter initialEntries={["/privado"]}>
      <Routes>
        <Route path="/login" element={<p>Tela de login</p>} />
        <Route
          path="/privado"
          element={
            <RotaProtegida>
              <p>Conteudo privado</p>
            </RotaProtegida>
          }
        />
      </Routes>
    </MemoryRouter>,
  );
}

describe("RotaProtegida", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  afterEach(() => {
    limparToken();
  });

  it("sem sessao, redireciona para /login", () => {
    renderComRota();

    expect(screen.getByText("Tela de login")).toBeInTheDocument();
    expect(screen.queryByText("Conteudo privado")).not.toBeInTheDocument();
  });

  it("com sessao valida, mostra o conteudo protegido", () => {
    salvarSessao("jwt-valido", "Nome", "EDITOR");

    renderComRota();

    expect(screen.getByText("Conteudo privado")).toBeInTheDocument();
  });
});
