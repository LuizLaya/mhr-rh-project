/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}

// Definido em vite.config.ts a partir da versão em package.json - usado na tela "Versão".
declare const __APP_VERSION__: string;
