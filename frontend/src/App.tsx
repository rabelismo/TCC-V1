import { useEffect, useState } from "react";
import ModuleSidebar from "./components/ModuleSidebar";
import ModuleView from "./components/ModuleView";
import { fetchModules } from "./services/api";
import type { Module } from "./types";

export default function App() {
  const [modules, setModules] = useState<Module[]>([]);
  const [activeModuleId, setActiveModuleId] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    fetchModules()
      .then((data) => {
        setModules(data);
        if (data.length > 0) setActiveModuleId(data[0].id);
      })
      .catch(() =>
        setError("Não foi possível carregar os módulos. Verifique se o backend está rodando.")
      )
      .finally(() => setLoading(false));
  }, []);

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
      />
      <main className="app-main">
        {activeModule ? (
          <ModuleView key={activeModule.id} module={activeModule} />
        ) : (
          <div className="empty-state">
            <p>Selecione um módulo para começar.</p>
          </div>
        )}
      </main>
    </div>
  );
}
