import { useCallback, useEffect, useState } from "react";
import ModuleSidebar from "./components/ModuleSidebar";
import ModuleView from "./components/ModuleView";
import AuthPage from "./components/AuthPage";
import { fetchModules, fetchUserProgress } from "./services/api";
import type { Module, StudentProgress, AuthUser } from "./types";

function loadStoredUser(): AuthUser | null {
  try {
    const stored = localStorage.getItem("tcc_auth");
    if (!stored) return null;
    return JSON.parse(stored) as AuthUser;
  } catch {
    return null;
  }
}

export default function App() {
  const [user, setUser] = useState<AuthUser | null>(loadStoredUser);
  const [modules, setModules] = useState<Module[]>([]);
  const [activeModuleId, setActiveModuleId] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
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

  useEffect(() => {
    fetchModules()
      .then((data) => {
        setModules(data);
        if (data.length > 0) setActiveModuleId(data[0].id);
      })
      .catch(() =>
        setError(
          "Não foi possível carregar os módulos. Verifique se o backend está rodando."
        )
      )
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    if (user) loadProgress();
  }, [user, loadProgress]);

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

  if (!user) {
    return <AuthPage onAuth={handleAuth} />;
  }

  const activeModule = modules.find((m) => m.id === activeModuleId) ?? null;

  if (loading) {
    return (
      <div className="loading-screen" role="status" aria-live="polite">
        <div className="spinner" />
        <p>Carregando módulos...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="error-screen">
        <h2>Ops!</h2>
        <p>{error}</p>
      </div>
    );
  }

  return (
    <div className="app-layout">
      <ModuleSidebar
        modules={modules}
        activeModuleId={activeModuleId}
        onSelectModule={setActiveModuleId}
        completedModuleIds={completedModuleIds}
        userName={user.name}
        onLogout={handleLogout}
      />
      <main className="app-main">
        {activeModule ? (
          <ModuleView
            key={activeModule.id}
            module={activeModule}
            userId={user.userId}
            onCompleted={handleModuleCompleted}
          />
        ) : (
          <div className="empty-state">
            <p>Selecione um módulo para começar.</p>
          </div>
        )}
      </main>
    </div>
  );
}
