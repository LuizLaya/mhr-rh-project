import { afterEach } from "vitest";
import { cleanup } from "@testing-library/react";
import "@testing-library/jest-dom/vitest";

// Sem `test.globals: true` no vite.config.ts, o Testing Library não acha um
// `afterEach` global para registrar sua limpeza automática - sem isso, o DOM
// de um teste fica de pé para o próximo teste do mesmo arquivo (`render()`
// empilha, nunca desmonta), causando falsos "elemento duplicado" em queries
// como `getByRole`.
afterEach(() => {
  cleanup();
});

// Com a combinação de versões deste projeto (jsdom 25 + Node 26), o
// `window.localStorage` do jsdom não fica disponível dentro do Vitest (bug de
// interação entre o jsdom e o localStorage experimental do próprio Node) -
// mesmo fora dos testes, em um Node isolado, o jsdom funciona normalmente.
// Para os testes não dependerem desse detalhe de ambiente, um polyfill mínimo
// (mesma interface de Storage) cobre o que o código da aplicação usa
// (getItem/setItem/removeItem/clear), tanto em `localStorage` quanto em
// `window.localStorage`.
class MemoryStorage implements Storage {
  private dados = new Map<string, string>();

  get length(): number {
    return this.dados.size;
  }

  clear(): void {
    this.dados.clear();
  }

  getItem(chave: string): string | null {
    return this.dados.has(chave) ? this.dados.get(chave)! : null;
  }

  key(indice: number): string | null {
    return [...this.dados.keys()][indice] ?? null;
  }

  removeItem(chave: string): void {
    this.dados.delete(chave);
  }

  setItem(chave: string, valor: string): void {
    this.dados.set(chave, valor);
  }
}

if (!window.localStorage || typeof window.localStorage.setItem !== "function") {
  const storage = new MemoryStorage();
  Object.defineProperty(window, "localStorage", { value: storage, configurable: true });
  Object.defineProperty(globalThis, "localStorage", { value: storage, configurable: true });
}
