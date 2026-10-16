import { useCallback, useEffect, useState } from "react";
import { AlertTriangle, RotateCw } from "lucide-react";
import ModuleSidebar from "./components/ModuleSidebar";
import ModuleView from "./components/ModuleView";
import AuthPage from "./components/AuthPage";
import { fetchModules, fetchUserProgress } from "./services/api";
import type { Module, StudentProgress, AuthUser } from "./types";

const MODULE_PARAM = "modulo";

function loadStoredUser(): AuthUser | null {
  try {
    const stored = localStorage.getItem("tcc_auth");
    if (!stored) return null;
    return JSON.parse(stored) as AuthUser;
  } catch {
    return null;
  }
}

function readModuleFromUrl(): number | null {
  const raw = new URLSearchParams(window.location.search).get(MODULE_PARAM);
  const id = raw ? Number(raw) : NaN;
  return Number.isFinite(id) ? id : null;
}

function LoadingSkeleton() {
  return (
    <div className="skeleton-layout" role="status" aria-live="polite">
      <span className="visually-hidden">Carregando módulos…</span>
      <div className="skeleton-sidebar" aria-hidden="true">
        <div className="skeleton" style={{ height: 30, width: "60%" }} />
        <div className="skeleton" style={{ height: 56, marginBottom: 8 }} />
        {Array.from({ length: 7 }, (_, i) => (
          <div key={i} className="skeleton" style={{ height: 40 }} />
        ))}
      </div>
      <div className="skeleton-main" aria-hidden="true">
        <div className="skeleton" style={{ height: 12, width: 160 }} />
        <div className="skeleton" style={{ height: 26, width: "42%" }} />
        <div className="skeleton" style={{ height: 14, width: "58%" }} />
        <div className="skeleton-split">
          <div className="skeleton-col">
            {[92, 100, 84, 96, 70].map((w, i) => (
              <div key={i} className="skeleton" style={{ height: 13, width: `${w}%` }} />
            ))}
          </div>
          <div className="skeleton-col">
            <div className="skeleton" style={{ flex: 1 }} />
          </div>
        </div>
      </div>
    </div>
  );
}

export default function App() {
  const [user, setUser] = useState<AuthUser | null>(loadStoredUser);
  const [modules, setModules] = useState<Module[]>([]);
  const [activeModuleId, setActiveModuleId] = useState<number | null>(readModuleFromUrl);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [completedModuleIds, setCompletedModuleIds] = useState<Set<number>>(
    new Set()
  );

  const loadProgress = useCallback(async () => {
    if (!user) return;
    try {
      const progress = await fetchUserProgress(user.userId);
      const completed = new Set(
        progress
          .filter((p: StudentProgress) => p.status === "COMPLETED")
          .map((p: StudentProgress) => p.module.id)
      );
      setCompletedModuleIds(completed);
    } catch {
      // progress is optional
    }
  }, [user]);

  const loadModules = useCallback(() => {
    setLoading(true);
    setError("");
    fetchModules()
      .then((data) => {
        setModules(data);
        setActiveModuleId((current) =>
          current !== null && data.some((m) => m.id === current)
            ? current
            : data[0]?.id ?? null
        );
      })
      .catch(() =>
        setError(
          "O servidor não respondeu. Confira sua conexão e tente de novo em alguns segundos."
        )
      )
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    loadModules();
  }, [loadModules]);

  useEffect(() => {
    if (user) loadProgress();
  }, [user, loadProgress]);

  useEffect(() => {
    if (activeModuleId === null) return;
    const url = new URL(window.location.href);
    url.searchParams.set(MODULE_PARAM, String(activeModuleId));
    window.history.replaceState(null, "", url);
  }, [activeModuleId]);

  useEffect(() => {
    if (!sidebarOpen) return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Escape") setSidebarOpen(false);
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [sidebarOpen]);

  const handleAuth = useCallback((authedUser: AuthUser) => {
    setUser(authedUser);
  }, []);

  const handleLogout = useCallback(() => {
    localStorage.removeItem("tcc_auth");
    setUser(null);
    setCompletedModuleIds(new Set());
  }, []);

  const handleModuleCompleted = useCallback((moduleId: number) => {
    setCompletedModuleIds((prev) => new Set([...prev, moduleId]));
  }, []);

  const handleSelectModule = useCallback((id: number) => {
    setActiveModuleId(id);
    setSidebarOpen(false);
  }, []);

  if (!user) {
    return <AuthPage onAuth={handleAuth} />;
  }

  const activeModule = modules.find((m) => m.id === activeModuleId) ?? null;

  if (loading) {
    return <LoadingSkeleton />;
  }

  if (error) {
    return (
      <main className="status-screen">
        <div className="status-card" role="alert">
          <span className="status-icon" aria-hidden="true">
            <AlertTriangle size={20} strokeWidth={1.75} />
          </span>
          <h1>Não foi possível carregar os módulos</h1>
          <p>{error}</p>
          <button type="button" className="btn btn-submit" onClick={loadModules}>
            <RotateCw size={15} strokeWidth={2} aria-hidden="true" />
            Tentar novamente
          </button>
        </div>
      </main>
    );
  }

  return (
    <div className="app-layout">
      <a className="skip-link" href="#conteudo">
        Pular para o conteúdo
      </a>
      <ModuleSidebar
        modules={modules}
        activeModuleId={activeModuleId}
        onSelectModule={handleSelectModule}
        completedModuleIds={completedModuleIds}
        userName={user.name}
        onLogout={handleLogout}
        isOpen={sidebarOpen}
        onClose={() => setSidebarOpen(false)}
      />
      <button
        type="button"
        className={`sidebar-backdrop ${sidebarOpen ? "open" : ""}`}
        aria-label="Fechar lista de módulos"
        tabIndex={-1}
        onClick={() => setSidebarOpen(false)}
      />
      <main className="app-main" id="conteudo">
        {activeModule ? (
          <ModuleView
            key={activeModule.id}
            module={activeModule}
            totalModules={modules.length}
            onCompleted={handleModuleCompleted}
            onOpenSidebar={() => setSidebarOpen(true)}
          />
        ) : (
          <div className="empty-state">
            <p>Nenhum módulo disponível ainda.</p>
          </div>
        )}
      </main>
    </div>
  );
}
