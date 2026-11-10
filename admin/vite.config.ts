import { readFileSync } from "node:fs";
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";

const { version } = JSON.parse(readFileSync(new URL("./package.json", import.meta.url), "utf-8"));

export default defineConfig({
  plugins: [react(), tailwindcss()],
  // Exposto como __APP_VERSION__ (ver vite-env.d.ts) - usado na tela "Versão".
  define: {
    __APP_VERSION__: JSON.stringify(version),
  },
  server: {
    port: 5173,
  },
  test: {
    environment: "jsdom",
    environmentOptions: {
      jsdom: { url: "http://localhost" },
    },
    setupFiles: ["./src/test/setup.ts"],
  },
});
