import { useMemo } from "react";
import { BookOpen, CheckCircle, LogOut } from "lucide-react";
import type { Module } from "../types";

interface ModuleSidebarProps {
  modules: Module[];
  activeModuleId: number | null;
  onSelectModule: (id: number) => void;
  completedModuleIds: Set<number>;
  userName: string;
  onLogout: () => void;
}

const conceptIcons: Record<string, string> = {
  STRINGS: "\" \"",
  INPUT_VARIABLES: "x =",
  ARRAYS_1D: "[ ]",
  ARRAYS_2D: "[[ ]]",
  FUNCTIONS: "def",
  STATE_MUTATION: ":=",
  TYPE_CASTING: "int",
  IF_ELSE: "if",
  LOGIC_OPERATORS: "&&",
  WHILE_LOOP: "whl",
  INPUT_VALIDATION: "try",
  AND_OPERATOR: "and",
  FOR_LOOP: "for",
  BREAK_STATEMENT: "brk",
  VARIABLES: "x = 1",
  ARRAYS: "[ ]",
  CONTROL_FLOW: "if",
  LOOPS: "for",
};

interface BlockDef {
  label: string;
  range: [number, number];
}

const blocks: BlockDef[] = [
  { label: "O Básico da Comunicação", range: [1, 2] },
  { label: "Desenhando o Campo de Batalha", range: [3, 5] },
  { label: "Interação e Estado", range: [6, 7] },
  { label: "O Juiz da Partida", range: [8, 9] },
  { label: "O Motor do Jogo", range: [10, 11] },
  { label: "O Algoritmo de Vitória", range: [12, 14] },
];

function getBlockForModule(orderIndex: number): BlockDef | undefined {
  return blocks.find(
    (b) => orderIndex >= b.range[0] && orderIndex <= b.range[1]
  );
}

interface GroupedItem {
  mod: Module;
  showBlockHeader: boolean;
  blockLabel: string;
  isCompleted: boolean;
}

export default function ModuleSidebar({
  modules,
  activeModuleId,
  onSelectModule,
  completedModuleIds,
  userName,
  onLogout,
}: ModuleSidebarProps) {
  const items = useMemo<GroupedItem[]>(() => {
    const sorted = [...modules].sort((a, b) => a.orderIndex - b.orderIndex);
    let lastLabel = "";
    return sorted.map((mod) => {
      const block = getBlockForModule(mod.orderIndex);
      const blockLabel = block?.label ?? "";
      const showBlockHeader = blockLabel !== lastLabel;
      lastLabel = blockLabel;
      return {
        mod,
        showBlockHeader,
        blockLabel,
        isCompleted: completedModuleIds.has(mod.id),
      };
    });
  }, [modules, completedModuleIds]);

  const totalCompleted = completedModuleIds.size;
  const totalModules = modules.length;

  return (
    <aside className="module-sidebar">
      <div className="sidebar-header">
        <BookOpen size={20} />
        <h2>Módulos</h2>
      </div>

      <div className="sidebar-user">
        <span className="sidebar-user-name" title={userName}>{userName}</span>
        <button className="sidebar-logout" onClick={onLogout} title="Sair">
          <LogOut size={16} />
        </button>
      </div>

      {totalModules > 0 && (
        <div className="sidebar-progress-summary">
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
              style={{
                width: `${(totalCompleted / totalModules) * 100}%`,
              }}
            />
          </div>
          <span className="progress-label">
            {totalCompleted}/{totalModules} concluídos
          </span>
        </div>
      )}

      <nav className="sidebar-nav" aria-label="Lista de módulos">
        {items.map(({ mod, showBlockHeader, blockLabel, isCompleted }) => (
          <div key={mod.id}>
            {showBlockHeader && blockLabel && (
              <div className="sidebar-block-header">
                <span className="sidebar-block-label">{blockLabel}</span>
              </div>
            )}
            <button
              className={`sidebar-item ${mod.id === activeModuleId ? "active" : ""} ${isCompleted ? "completed" : ""}`}
              onClick={() => onSelectModule(mod.id)}
              aria-current={mod.id === activeModuleId ? "true" : undefined}
            >
              <span className="sidebar-item-badge">
                {isCompleted ? (
                  <CheckCircle size={18} className="check-icon" />
                ) : (
                  conceptIcons[mod.concept] ?? mod.orderIndex
                )}
              </span>
              <div className="sidebar-item-content">
                <span className="sidebar-item-title">{mod.title}</span>
                <span className="sidebar-item-concept">{mod.concept}</span>
              </div>
            </button>
          </div>
        ))}
      </nav>
    </aside>
  );
}
