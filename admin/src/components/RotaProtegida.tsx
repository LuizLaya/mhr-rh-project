import type { ReactNode } from "react";
import { Navigate } from "react-router";
import { estaAutenticado } from "../lib/auth";

/** Redireciona para /login quando não há um token JWT salvo. */
export default function RotaProtegida({ children }: { children: ReactNode }) {
  if (!estaAutenticado()) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
}
