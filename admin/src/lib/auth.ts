/** Sessão do usuário logado no painel: token JWT + dados exibidos na UI (nome, papel). */

export type Papel = "ADMIN" | "EDITOR";

const CHAVE_TOKEN = "mhr_admin_token";
const CHAVE_NOME = "mhr_admin_nome";
const CHAVE_PAPEL = "mhr_admin_papel";

/** Chamado uma vez, logo após o POST /api/auth/login responder com sucesso. */
export function salvarSessao(token: string, nome: string, papel: Papel) {
  localStorage.setItem(CHAVE_TOKEN, token);
  localStorage.setItem(CHAVE_NOME, nome);
  localStorage.setItem(CHAVE_PAPEL, papel);
}

export function obterToken(): string | null {
  return localStorage.getItem(CHAVE_TOKEN);
}

export function obterNome(): string | null {
  return localStorage.getItem(CHAVE_NOME);
}

export function obterPapel(): Papel | null {
  return localStorage.getItem(CHAVE_PAPEL) as Papel | null;
}

export function ehAdmin(): boolean {
  return obterPapel() === "ADMIN";
}

export function limparToken() {
  localStorage.removeItem(CHAVE_TOKEN);
  localStorage.removeItem(CHAVE_NOME);
  localStorage.removeItem(CHAVE_PAPEL);
}

export function estaAutenticado(): boolean {
  return obterToken() !== null;
}
