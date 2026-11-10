import { getViteConfig } from "astro/config";

// getViteConfig reaproveita o vite de astro.config.mjs (plugin do React, Tailwind)
// - sem isso, os testes de componente React (.tsx) nao teriam o JSX transformado.
export default getViteConfig({
  test: {
    environment: "jsdom",
    environmentOptions: {
      jsdom: { url: "http://localhost" },
    },
    setupFiles: ["./src/test/setup.ts"],
  },
});
