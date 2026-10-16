import { useEffect, useMemo, useRef } from "react";
import { Check, Hash, LogOut, X } from "lucide-react";
import type { Module } from "../types";
import { conceptIcons, getBlockForModule } from "../curriculum";

interface ModuleSidebarProps {
  modules: Module[];
  activeModuleId: number | null;
  onSelectModule: (id: number) => void;
  completedModuleIds: Set<number>;
  userName: string;
  onLogout: () => void;
  isOpen?: boolean;
  onClose?: () => void;
}

interface ModuleGroup {
  label: string;
  items: { mod: Module; isCompleted: boolean }[];
}

function initialsOf(name: string): string {
  const parts = name.trim().split(/\s+/).filter(Boolean);
  if (parts.length === 0) return "?";
  const first = parts[0][0];
  const last = parts.length > 1 ? parts[parts.length - 1][0] : "";
  return (first + last).toUpperCase();
}

export default function ModuleSidebar({
  modules,
  activeModuleId,
  onSelectModule,
  completedModuleIds,
  userName,
  onLogout,
  isOpen = false,
  onClose,
}: ModuleSidebarProps) {
  const closeRef = useRef<HTMLButtonElement>(null);

  const groups = useMemo<ModuleGroup[]>(() => {
    const sorted = [...modules].sort((a, b) => a.orderIndex - b.orderIndex);
    const result: ModuleGroup[] = [];
    for (const mod of sorted) {
      const label = getBlockForModule(mod.orderIndex)?.label ?? "";
      const last = result[result.length - 1];
      const item = { mod, isCompleted: completedModuleIds.has(mod.id) };
      if (last && last.label === label) last.items.push(item);
      else result.push({ label, items: [item] });
    }
    return result;
  }, [modules, completedModuleIds]);

  useEffect(() => {
    if (isOpen) closeRef.current?.focus();
  }, [isOpen]);

  const totalCompleted = completedModuleIds.size;
  const totalModules = modules.length;
  const ratio = totalModules > 0 ? totalCompleted / totalModules : 0;

  return (
    <aside
      className={`module-sidebar ${isOpen ? "open" : ""}`}
      aria-label="Módulos do curso"
    >
      <div className="sidebar-header">
        <span className="sidebar-brand-mark" aria-hidden="true">
          <Hash size={17} strokeWidth={2} />
        </span>
        <div className="sidebar-brand">
          <span className="sidebar-brand-title">Jogo da Velha</span>
          <span className="sidebar-brand-sub">Python, passo a passo</span>
        </div>
        {onClose && (
          <button
            ref={closeRef}
            type="button"
            className="icon-btn sidebar-close"
            onClick={onClose}
            aria-label="Fechar lista de módulos"
          >
            <X size={18} strokeWidth={1.75} aria-hidden="true" />
          </button>
        )}
      </div>

      {totalModules > 0 && (
        <div className="sidebar-progress-summary">
          <div className="progress-row">
            <span className="progress-caption">Seu progresso</span>
            <span className="progress-label">
              {totalCompleted}/{totalModules} concluídos
            </span>
          </div>
          <div
            className="progress-bar-track"
            role="progressbar"
            aria-valuenow={totalCompleted}
            aria-valuemin={0}
            aria-valuemax={totalModules}
            aria-label={`${totalCompleted} de ${totalModules} módulos concluídos`}
          >
            <div
              className="progress-bar-fill"
              style={{ transform: `scaleX(${ratio})` }}
            />
          </div>
        </div>
      )}

      <nav className="sidebar-nav" aria-label="Lista de módulos">
        <ul className="sidebar-groups">
          {groups.map((group) => (
            <li key={group.label || "outros"} className="sidebar-group">
              {group.label && (
                <div className="sidebar-block-header">
                  <span className="sidebar-block-label">{group.label}</span>
                </div>
              )}
              <ul className="sidebar-group-items">
                {group.items.map(({ mod, isCompleted }) => {
                  const isActive = mod.id === activeModuleId;
                  return (
                    <li key={mod.id}>
                      <button
                        type="button"
                        className={`sidebar-item ${isActive ? "active" : ""} ${isCompleted ? "completed" : ""}`}
                        onClick={() => onSelectModule(mod.id)}
                        aria-current={isActive ? "step" : undefined}
                      >
                        <span className="sidebar-item-badge" aria-hidden="true">
                          {isCompleted ? (
                            <Check size={16} strokeWidth={2.25} className="check-icon" />
                          ) : (
                            conceptIcons[mod.concept] ?? mod.orderIndex
                          )}
                        </span>
                        <span className="sidebar-item-content">
                          <span className="sidebar-item-title">{mod.title}</span>
                          <span className="sidebar-item-concept" translate="no">
                            {mod.concept.toLowerCase()}
                          </span>
                        </span>
                        {isCompleted && <span className="visually-hidden">(concluído)</span>}
                      </button>
                    </li>
                  );
                })}
              </ul>
            </li>
          ))}
        </ul>
      </nav>

      <div className="sidebar-user">
        <span className="sidebar-avatar" aria-hidden="true">
          {initialsOf(userName)}
        </span>
        <span className="sidebar-user-name" title={userName}>
          {userName}
        </span>
        <button
          type="button"
          className="icon-btn sidebar-logout"
          onClick={onLogout}
          title="Sair"
          aria-label="Sair da conta"
        >
          <LogOut size={16} strokeWidth={1.75} aria-hidden="true" />
        </button>
      </div>
    </aside>
  );
}
