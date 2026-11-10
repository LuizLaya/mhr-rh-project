import { Suspense, lazy } from "react";
import { Routes, Route } from "react-router";
import LoginPage from "./pages/LoginPage";
import EsqueciSenhaPage from "./pages/EsqueciSenhaPage";
import RedefinirSenhaPage from "./pages/RedefinirSenhaPage";
import AceitarConvitePage from "./pages/AceitarConvitePage";
import AdminLayout from "./components/AdminLayout";
import RotaProtegida from "./components/RotaProtegida";
import RotaAdmin from "./components/RotaAdmin";

// Code-splitting: cada tela do admin (fora do login) vira seu proprio chunk JS,
// baixado so quando o admin navega ate ela - o bundle inicial nao carrega mais
// (nem o navegador baixa) as ~15 telas de uma vez so, so a que esta sendo usada.
const DashboardPage = lazy(() => import("./pages/DashboardPage"));
const VagasPage = lazy(() => import("./pages/VagasPage"));
const CandidaturasPage = lazy(() => import("./pages/CandidaturasPage"));
const ServicosPage = lazy(() => import("./pages/ServicosPage"));
const DepoimentosPage = lazy(() => import("./pages/DepoimentosPage"));
const EquipePage = lazy(() => import("./pages/EquipePage"));
const FaqPage = lazy(() => import("./pages/FaqPage"));
const PostsBlogPage = lazy(() => import("./pages/PostsBlogPage"));
const LeadsContatoPage = lazy(() => import("./pages/LeadsContatoPage"));
const ClientesPage = lazy(() => import("./pages/ClientesPage"));
const LicitacoesPage = lazy(() => import("./pages/LicitacoesPage"));
const UsuariosPage = lazy(() => import("./pages/UsuariosPage"));
const LogsPage = lazy(() => import("./pages/LogsPage"));
const VersaoPage = lazy(() => import("./pages/VersaoPage"));

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/esqueci-senha" element={<EsqueciSenhaPage />} />
      <Route path="/redefinir-senha" element={<RedefinirSenhaPage />} />
      <Route path="/aceitar-convite" element={<AceitarConvitePage />} />
      <Route
        path="/*"
        element={
          <RotaProtegida>
            <AdminLayout />
          </RotaProtegida>
        }
      >
        <Route
          index
          element={
            <Suspense fallback={<p>Carregando...</p>}>
              <DashboardPage />
            </Suspense>
          }
        />
        <Route
          path="vagas"
          element={
            <Suspense fallback={<p>Carregando...</p>}>
              <VagasPage />
            </Suspense>
          }
        />
        <Route
          path="candidaturas"
          element={
            <Suspense fallback={<p>Carregando...</p>}>
              <CandidaturasPage />
            </Suspense>
          }
        />
        <Route
          path="servicos"
          element={
            <Suspense fallback={<p>Carregando...</p>}>
              <ServicosPage />
            </Suspense>
          }
        />
        <Route
          path="depoimentos"
          element={
            <Suspense fallback={<p>Carregando...</p>}>
              <DepoimentosPage />
            </Suspense>
          }
        />
        <Route
          path="equipe"
          element={
            <Suspense fallback={<p>Carregando...</p>}>
              <EquipePage />
            </Suspense>
          }
        />
        <Route
          path="faq"
          element={
            <Suspense fallback={<p>Carregando...</p>}>
              <FaqPage />
            </Suspense>
          }
        />
        <Route
          path="blog"
          element={
            <Suspense fallback={<p>Carregando...</p>}>
              <PostsBlogPage />
            </Suspense>
          }
        />
        <Route
          path="leads"
          element={
            <Suspense fallback={<p>Carregando...</p>}>
              <LeadsContatoPage />
            </Suspense>
          }
        />
        <Route
          path="clientes"
          element={
            <Suspense fallback={<p>Carregando...</p>}>
              <ClientesPage />
            </Suspense>
          }
        />
        <Route
          path="licitacoes"
          element={
            <Suspense fallback={<p>Carregando...</p>}>
              <LicitacoesPage />
            </Suspense>
          }
        />
        <Route
          path="usuarios"
          element={
            <RotaAdmin>
              <Suspense fallback={<p>Carregando...</p>}>
                <UsuariosPage />
              </Suspense>
            </RotaAdmin>
          }
        />
        <Route
          path="logs"
          element={
            <RotaAdmin>
              <Suspense fallback={<p>Carregando...</p>}>
                <LogsPage />
              </Suspense>
            </RotaAdmin>
          }
        />
        <Route
          path="versao"
          element={
            <RotaAdmin>
              <Suspense fallback={<p>Carregando...</p>}>
                <VersaoPage />
              </Suspense>
            </RotaAdmin>
          }
        />
      </Route>
    </Routes>
  );
}
