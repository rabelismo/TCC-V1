import { BookOpen, CheckCircle2, Circle, PlayCircle } from "lucide-react";
import type { Module } from "../types";

interface ModuleSidebarProps {
  modules: Module[];
  activeModuleId: number | null;
  onSelectModule: (id: number) => void;
}

const conceptIcons: Record<string, string> = {
  VARIABLES: "x = 1",
  ARRAYS: "[ ]",
  CONTROL_FLOW: "if",
  LOOPS: "for",
};

export default function ModuleSidebar({
  modules,
  activeModuleId,
  onSelectModule,
}: ModuleSidebarProps) {
  return (
    <aside className="module-sidebar">
      <div className="sidebar-header">
        <BookOpen size={20} />
        <h2>Módulos</h2>
      </div>
      <nav className="sidebar-nav">
        {modules.map((mod) => (
          <button
            key={mod.id}
            className={`sidebar-item ${mod.id === activeModuleId ? "active" : ""}`}
            onClick={() => onSelectModule(mod.id)}
          >
            <span className="sidebar-item-badge">
              {conceptIcons[mod.concept] ?? mod.orderIndex}
            </span>
            <div className="sidebar-item-content">
              <span className="sidebar-item-title">{mod.title}</span>
              <span className="sidebar-item-concept">{mod.concept}</span>
            </div>
          </button>
        ))}
      </nav>
    </aside>
  );
}
