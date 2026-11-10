import type { ReactNode } from "react";
import { Navigate } from "react-router";
import { ehAdmin } from "../lib/auth";

/**
 * Esconde telas de ADMIN de um EDITOR logado (redireciona para a home). Só UX: a
 * autorização de verdade é no backend (`@PreAuthorize("hasRole('ADMIN')")` em
 * UsuarioAdminController) — este guard não é, sozinho, a barreira de segurança.
 */
export default function RotaAdmin({ children }: { children: ReactNode }) {
  if (!ehAdmin()) {
    return <Navigate to="/" replace />;
  }
  return <>{children}</>;
}
