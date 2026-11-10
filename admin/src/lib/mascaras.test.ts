import { describe, expect, it } from "vitest";
import { mascararCnpj, mascararTelefone } from "./mascaras";

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
});

describe("mascararCnpj", () => {
  it("formata um CNPJ completo", () => {
    expect(mascararCnpj("57776966000132")).toBe("57.776.966/0001-32");
  });

  it("formata parcialmente enquanto o usuário ainda está digitando", () => {
    expect(mascararCnpj("5777")).toBe("57.77");
    expect(mascararCnpj("57776966")).toBe("57.776.966");
  });

  it("corta em 14 dígitos", () => {
    expect(mascararCnpj("577769660001329999")).toBe("57.776.966/0001-32");
  });
});
