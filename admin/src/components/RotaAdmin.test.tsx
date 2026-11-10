import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router";
import RotaAdmin from "./RotaAdmin";
import { limparToken, salvarSessao } from "../lib/auth";

function renderComRota() {
  return render(
    <MemoryRouter initialEntries={["/usuarios"]}>
      <Routes>
        <Route path="/" element={<p>Dashboard</p>} />
        <Route
          path="/usuarios"
          element={
            <RotaAdmin>
              <p>Gestao de usuarios</p>
            </RotaAdmin>
          }
        />
      </Routes>
    </MemoryRouter>,
  );
}

describe("RotaAdmin", () => {
  afterEach(() => {
    limparToken();
  });

  beforeEach(() => {
    localStorage.clear();
  });

  it("usuario EDITOR e redirecionado para a home, nao ve a tela de ADMIN", () => {
    salvarSessao("jwt", "Nome", "EDITOR");

    renderComRota();

    expect(screen.getByText("Dashboard")).toBeInTheDocument();
    expect(screen.queryByText("Gestao de usuarios")).not.toBeInTheDocument();
  });

  it("usuario ADMIN consegue ver a tela restrita", () => {
    salvarSessao("jwt", "Nome", "ADMIN");

    renderComRota();

    expect(screen.getByText("Gestao de usuarios")).toBeInTheDocument();
  });
});
