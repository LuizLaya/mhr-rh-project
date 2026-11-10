import { afterEach } from "vitest";
import { cleanup } from "@testing-library/react";
import "@testing-library/jest-dom/vitest";

// Sem `test.globals: true` no vitest.config.ts, o Testing Library não acha um
// `afterEach` global para registrar sua limpeza automática - sem isso, o DOM
// de um teste fica de pé para o próximo teste do mesmo arquivo.
afterEach(() => {
  cleanup();
});
