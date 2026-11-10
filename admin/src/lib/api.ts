const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

function authHeaders(): HeadersInit {
  const token = localStorage.getItem("mhr_admin_token");
  return token ? { Authorization: `Bearer ${token}` } : {};
}

/**
 * Wrapper de fetch para a API admin: injeta o Authorization: Bearer e já lança um
 * Error com a mensagem que o backend devolveu (ApiExceptionHandler sempre manda
 * {"message": "..."}), em vez de só o código HTTP — é essa mensagem que as telas
 * mostram para o usuário (ex.: "Ja existe um usuario cadastrado com esse e-mail").
 */
export async function apiFetch<T>(path: string, init?: RequestInit): Promise<T> {
  // FormData (upload de arquivo) precisa que o proprio navegador defina o
  // Content-Type (com o boundary do multipart) - forcar application/json quebraria
  // o envio.
  const ehFormData = init?.body instanceof FormData;
  const res = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers: {
      ...(ehFormData ? {} : { "Content-Type": "application/json" }),
      ...authHeaders(),
      ...init?.headers,
    },
  });

  if (!res.ok) {
    const corpo = await res.json().catch(() => null);
    throw new Error(corpo?.message ?? `Erro na API: ${res.status}`);
  }

  if (res.status === 204) {
    return undefined as T;
  }

  return res.json();
}
