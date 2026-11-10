import { beforeAll, describe, expect, it } from "vitest";
import { z } from "zod";
import { instalarMensagensZodPtBr } from "./zod-pt-br";

describe("instalarMensagensZodPtBr", () => {
  beforeAll(() => {
    instalarMensagensZodPtBr();
  });

  it("campo obrigatorio ausente vira 'Campo obrigatório'", () => {
    const schema = z.object({ nome: z.string() });

    const resultado = schema.safeParse({});

    expect(resultado.success).toBe(false);
    if (!resultado.success) {
      expect(resultado.error.issues[0].message).toBe("Campo obrigatório");
    }
  });

  it("e-mail invalido vira 'E-mail inválido'", () => {
    const schema = z.object({ email: z.string().email() });

    const resultado = schema.safeParse({ email: "nao-e-um-email" });

    expect(resultado.success).toBe(false);
    if (!resultado.success) {
      expect(resultado.error.issues[0].message).toBe("E-mail inválido");
    }
  });

  it("string curta demais informa o minimo exigido", () => {
    const schema = z.object({ nome: z.string().min(3) });

    const resultado = schema.safeParse({ nome: "ab" });

    expect(resultado.success).toBe(false);
    if (!resultado.success) {
      expect(resultado.error.issues[0].message).toBe("Deve ter pelo menos 3 caracteres");
    }
  });
});
