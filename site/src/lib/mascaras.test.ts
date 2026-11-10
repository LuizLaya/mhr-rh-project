import { describe, expect, it } from "vitest";
import { mascararTelefone } from "./mascaras";

describe("mascararTelefone", () => {
  it("formata celular (11 dígitos) como (XX) XXXXX-XXXX", () => {
    expect(mascararTelefone("61990000000")).toBe("(61) 99000-0000");
  });

  it("formata fixo (10 dígitos) como (XX) XXXX-XXXX", () => {
    expect(mascararTelefone("6133330000")).toBe("(61) 3333-0000");
  });

  it("ignora caracteres não numéricos já presentes no valor", () => {
    expect(mascararTelefone("(61) 99000-0000")).toBe("(61) 99000-0000");
  });

  it("corta em 11 dígitos, ignorando o que vier depois", () => {
    expect(mascararTelefone("619900000001234")).toBe("(61) 99000-0000");
  });

  it("string vazia continua vazia", () => {
    expect(mascararTelefone("")).toBe("");
  });

  it("menos de 3 dígitos mostra só o DDD aberto", () => {
    expect(mascararTelefone("6")).toBe("(6");
  });
});
