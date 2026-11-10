import { beforeEach, describe, expect, it } from "vitest";
import {
  ehAdmin,
  estaAutenticado,
  limparToken,
  obterNome,
  obterPapel,
  obterToken,
  salvarSessao,
} from "./auth";

describe("auth", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it("salvarSessao grava token, nome e papel no localStorage", () => {
    salvarSessao("jwt-123", "Administrador MHR", "ADMIN");

    expect(obterToken()).toBe("jwt-123");
    expect(obterNome()).toBe("Administrador MHR");
    expect(obterPapel()).toBe("ADMIN");
  });

  it("estaAutenticado reflete se existe token salvo", () => {
    expect(estaAutenticado()).toBe(false);

    salvarSessao("jwt-123", "Nome", "EDITOR");

    expect(estaAutenticado()).toBe(true);
  });

  it("ehAdmin so e verdadeiro quando o papel salvo e ADMIN", () => {
    salvarSessao("jwt-123", "Nome", "EDITOR");
    expect(ehAdmin()).toBe(false);

    salvarSessao("jwt-456", "Nome", "ADMIN");
    expect(ehAdmin()).toBe(true);
  });

  it("limparToken remove token, nome e papel", () => {
    salvarSessao("jwt-123", "Nome", "ADMIN");

    limparToken();

    expect(obterToken()).toBeNull();
    expect(obterNome()).toBeNull();
    expect(obterPapel()).toBeNull();
    expect(estaAutenticado()).toBe(false);
  });
});
