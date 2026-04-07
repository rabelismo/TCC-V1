import { useCallback, useEffect, useState } from "react";
import ModuleSidebar from "./components/ModuleSidebar";
import ModuleView from "./components/ModuleView";
import { fetchModules, fetchUserProgress } from "./services/api";
import type { Module, StudentProgress } from "./types";

function getOrCreateUserId(): number {
  const key = "tcc_user_id";
  const stored = localStorage.getItem(key);
  if (stored) return Number(stored);
  const id = 1;
  localStorage.setItem(key, String(id));
  return id;
}

const USER_ID = getOrCreateUserId();

export default function App() {
  const [modules, setModules] = useState<Module[]>([]);
  const [activeModuleId, setActiveModuleId] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [completedModuleIds, setCompletedModuleIds] = useState<Set<number>>(
    new Set()
  );

  const loadProgress = useCallback(async () => {
    try {
      const progress = await fetchUserProgress(USER_ID);
      const completed = new Set(
        progress
          .filter((p: StudentProgress) => p.status === "COMPLETED")
          .map((p: StudentProgress) => p.module.id)
      );
      setCompletedModuleIds(completed);
    } catch {
      // progress is optional, don't block
    }
  }, []);

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

    loadProgress();
  }, [loadProgress]);

  const handleModuleCompleted = useCallback(
    (moduleId: number) => {
      setCompletedModuleIds((prev) => new Set([...prev, moduleId]));
    },
    []
  );

  const activeModule = modules.find((m) => m.id === activeModuleId) ?? null;

  if (loading) {
    return (
      <div className="loading-screen">
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
      />
      <main className="app-main">
        {activeModule ? (
          <ModuleView
            key={activeModule.id}
            module={activeModule}
            userId={USER_ID}
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
