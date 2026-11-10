const API_BASE_URL = import.meta.env.PUBLIC_API_BASE_URL;

/** Espelha o VagaDto do backend (br.com.mhr.backend.vaga). */
export interface Vaga {
  id: string;
  titulo: string;
  slug: string;
  descricao: string;
  cidade: string;
  modeloTrabalho: "PRESENCIAL" | "HIBRIDO" | "REMOTO";
  status: "ABERTA" | "ENCERRADA";
  publicadoEm: string | null;
}

export async function buscarVagas(): Promise<Vaga[]> {
  const res = await fetch(`${API_BASE_URL}/api/vagas`);
  if (!res.ok) throw new Error("Falha ao carregar vagas");
  return res.json();
}

export async function buscarVaga(id: string): Promise<Vaga> {
  const res = await fetch(`${API_BASE_URL}/api/vagas/${id}`);
  if (!res.ok) throw new Error("Vaga não encontrada");
  return res.json();
}

/** Espelha o ServicoDto do backend (br.com.mhr.backend.servico). */
export interface Servico {
  id: string;
  titulo: string;
  slug: string;
  resumo: string;
  conteudo: string;
  icone: string | null;
  ordemExibicao: number;
}

export async function buscarServicos(): Promise<Servico[]> {
  const res = await fetch(`${API_BASE_URL}/api/servicos`);
  if (!res.ok) throw new Error("Falha ao carregar servicos");
  return res.json();
}

export async function buscarServico(slug: string): Promise<Servico> {
  const res = await fetch(`${API_BASE_URL}/api/servicos/${slug}`);
  if (!res.ok) throw new Error("Servico nao encontrado");
  return res.json();
}

/** Espelha o DepoimentoDto do backend (br.com.mhr.backend.depoimento). */
export interface Depoimento {
  id: string;
  nomeAutor: string;
  cargoAutor: string | null;
  nomeEmpresa: string | null;
  conteudo: string;
  urlFoto: string | null;
}

export async function buscarDepoimentos(): Promise<Depoimento[]> {
  const res = await fetch(`${API_BASE_URL}/api/depoimentos`);
  if (!res.ok) throw new Error("Falha ao carregar depoimentos");
  return res.json();
}

/** Espelha o PostBlogDto do backend (br.com.mhr.backend.blog). */
export interface PostBlog {
  id: string;
  titulo: string;
  slug: string;
  resumo: string;
  conteudo: string;
  urlImagemCapa: string | null;
  categoria: string | null;
  publicadoEm: string | null;
}

export async function buscarPostsBlog(): Promise<PostBlog[]> {
  const res = await fetch(`${API_BASE_URL}/api/posts-blog`);
  if (!res.ok) throw new Error("Falha ao carregar posts do blog");
  return res.json();
}

/** Espelha o MembroEquipeDto do backend (br.com.mhr.backend.equipe). */
export interface MembroEquipe {
  id: string;
  nome: string;
  cargo: string | null;
  fotoUrl: string | null;
  bio: string | null;
  ordemExibicao: number;
}

export async function buscarEquipe(): Promise<MembroEquipe[]> {
  const res = await fetch(`${API_BASE_URL}/api/equipe`);
  if (!res.ok) throw new Error("Falha ao carregar a equipe");
  return res.json();
}

/** Espelha o FaqDto do backend (br.com.mhr.backend.faq). */
export interface Faq {
  id: string;
  pergunta: string;
  resposta: string;
  ordemExibicao: number;
}

export async function buscarFaq(): Promise<Faq[]> {
  const res = await fetch(`${API_BASE_URL}/api/faq`);
  if (!res.ok) throw new Error("Falha ao carregar as perguntas frequentes");
  return res.json();
}

/** Espelha o ClienteDestaqueDto do backend (br.com.mhr.backend.cliente) - só nome e logo, nunca dado sensível. */
export interface ClienteDestaque {
  nomeEmpresa: string;
  logoUrl: string | null;
}

export async function buscarClientesDestaque(): Promise<ClienteDestaque[]> {
  const res = await fetch(`${API_BASE_URL}/api/clientes-destaque`);
  if (!res.ok) throw new Error("Falha ao carregar os clientes em destaque");
  return res.json();
}
