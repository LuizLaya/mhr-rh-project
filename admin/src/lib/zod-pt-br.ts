import { z, ZodIssueCode } from "zod";

/**
 * Traduz as mensagens padrão do Zod (em inglês) para português, uma vez só, no boot
 * do app. Schemas continuam podendo sobrescrever com sua própria `message` quando
 * precisarem de um texto mais específico.
 */
export function instalarMensagensZodPtBr() {
  z.setErrorMap((issue, ctx) => {
    switch (issue.code) {
      case ZodIssueCode.invalid_type:
        if (issue.received === "undefined" || issue.received === "null") {
          return { message: "Campo obrigatório" };
        }
        break;
      case ZodIssueCode.too_small:
        if (issue.type === "string") {
          return {
            message: issue.minimum === 1
              ? "Campo obrigatório"
              : `Deve ter pelo menos ${issue.minimum} caracteres`,
          };
        }
        if (issue.type === "number") {
          return { message: `Deve ser maior ou igual a ${issue.minimum}` };
        }
        break;
      case ZodIssueCode.too_big:
        if (issue.type === "string") {
          return { message: `Deve ter no máximo ${issue.maximum} caracteres` };
        }
        if (issue.type === "number") {
          return { message: `Deve ser menor ou igual a ${issue.maximum}` };
        }
        break;
      case ZodIssueCode.invalid_string:
        if (issue.validation === "email") return { message: "E-mail inválido" };
        if (issue.validation === "url") return { message: "URL inválida" };
        break;
      case ZodIssueCode.invalid_enum_value:
        return { message: "Valor inválido" };
    }
    return { message: ctx.defaultError };
  });
}
