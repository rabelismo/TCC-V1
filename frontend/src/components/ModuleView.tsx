import { useCallback, useEffect, useRef, useState } from "react";
import { Menu, Play, RotateCcw, Send, Square } from "lucide-react";
import TheoryPanel from "./TheoryPanel";
import CodeEditor from "./CodeEditor";
import TerminalOutput from "./TerminalOutput";
import SubmissionFeedback from "./SubmissionFeedback";
import { executeCode, submitCode } from "../services/api";
import { getBlockForModule } from "../curriculum";
import type { CodeExecutionRequest, Module, SubmissionResult } from "../types";

const AUTOSAVE_KEY_PREFIX = "tcc_code_";
const AUTOSAVE_DELAY_MS = 800;
const RESET_CONFIRM_MS = 3500;

function loadSavedCode(moduleId: number, fallback: string): string {
  try {
    const saved = localStorage.getItem(`${AUTOSAVE_KEY_PREFIX}${moduleId}`);
    return saved ?? fallback;
  } catch {
    return fallback;
  }
}

function saveCode(moduleId: number, code: string): void {
  try {
    localStorage.setItem(`${AUTOSAVE_KEY_PREFIX}${moduleId}`, code);
  } catch {
    // quota exceeded — ignore
  }
}

function clearSavedCode(moduleId: number): void {
  try {
    localStorage.removeItem(`${AUTOSAVE_KEY_PREFIX}${moduleId}`);
  } catch {
    // ignore
  }
}

interface ModuleViewProps {
  module: Module;
  totalModules: number;
  userId: number;
  onCompleted: (moduleId: number) => void;
  onOpenSidebar: () => void;
}

export default function ModuleView({
  module,
  totalModules,
  userId,
  onCompleted,
  onOpenSidebar,
}: ModuleViewProps) {
  const [code, setCode] = useState(() =>
    loadSavedCode(module.id, module.starterCode)
  );
  const [stdinInput, setStdinInput] = useState(module.sampleInput ?? "");
  const [output, setOutput] = useState("");
  const [error, setError] = useState("");
  const [isRunning, setIsRunning] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [confirmingReset, setConfirmingReset] = useState(false);
  const [executionTimeMs, setExecutionTimeMs] = useState<number | undefined>();
  const [submissionResult, setSubmissionResult] = useState<SubmissionResult | null>(null);
  const abortRef = useRef<AbortController | null>(null);
  const saveTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const resetTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const block = getBlockForModule(module.orderIndex);
  const pad = (n: number) => String(n).padStart(2, "0");

  const debouncedSave = useCallback(
    (value: string) => {
      if (saveTimerRef.current) clearTimeout(saveTimerRef.current);
      saveTimerRef.current = setTimeout(() => {
        saveCode(module.id, value);
      }, AUTOSAVE_DELAY_MS);
    },
    [module.id]
  );

  useEffect(() => {
    return () => {
      if (saveTimerRef.current) clearTimeout(saveTimerRef.current);
      if (resetTimerRef.current) clearTimeout(resetTimerRef.current);
    };
  }, []);

  const handleCodeChange = (value: string) => {
    setCode(value);
    debouncedSave(value);
  };

  const handleRun = async () => {
    setIsRunning(true);
    setOutput("");
    setError("");
    setExecutionTimeMs(undefined);
    setSubmissionResult(null);

    const controller = new AbortController();
    abortRef.current = controller;

    try {
      const request: CodeExecutionRequest = { code };
      if (stdinInput.trim()) {
        request.input = stdinInput;
      }
      const result = await executeCode(request, controller.signal);
      setOutput(result.output);
      setError(result.error);
      setExecutionTimeMs(result.executionTimeMs);
    } catch {
      if (controller.signal.aborted) {
        setError("Execução interrompida.");
      } else {
        setError("O servidor não respondeu. Tente executar de novo em alguns segundos.");
      }
    } finally {
      abortRef.current = null;
      setIsRunning(false);
    }
  };

  const handleStop = () => {
    abortRef.current?.abort();
  };

  const handleSubmit = async () => {
    setIsSubmitting(true);
    setSubmissionResult(null);
    setError("");

    try {
      const result = await submitCode({ moduleId: module.id, userId, code });
      setSubmissionResult(result);
      if (result.allPassed) {
        onCompleted(module.id);
      }
    } catch {
      setError("Não foi possível avaliar seu código agora. Seu código está salvo — tente submeter de novo.");
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleReset = () => {
    if (!confirmingReset) {
      setConfirmingReset(true);
      resetTimerRef.current = setTimeout(() => setConfirmingReset(false), RESET_CONFIRM_MS);
      return;
    }
    if (resetTimerRef.current) clearTimeout(resetTimerRef.current);
    setConfirmingReset(false);
    setCode(module.starterCode);
    clearSavedCode(module.id);
    setStdinInput(module.sampleInput ?? "");
    setOutput("");
    setError("");
    setExecutionTimeMs(undefined);
    setSubmissionResult(null);
  };

  return (
    <div className="module-view">
      <header className="module-header">
        <button
          type="button"
          className="icon-btn module-menu-toggle"
          onClick={onOpenSidebar}
          aria-label="Abrir lista de módulos"
        >
          <Menu size={20} strokeWidth={1.75} aria-hidden="true" />
        </button>
        <div className="module-heading">
          <p className="module-eyebrow">
            <span className="module-eyebrow-index">
              {pad(module.orderIndex)}/{pad(totalModules)}
            </span>
            {block && (
              <>
                <span className="module-eyebrow-sep" aria-hidden="true" />
                <span>{block.label}</span>
              </>
            )}
          </p>
          <h1 className="module-title">{module.title}</h1>
          <p className="module-description">{module.description}</p>
        </div>
      </header>

      <div className="module-content">
        <section className="panel-theory" aria-label="Teoria">
          <TheoryPanel markdown={module.theoryMarkdown} />
        </section>

        <section className="panel-code" aria-label="Código">
          <div className="code-toolbar">
            <span className="code-toolbar-file" translate="no">main.py</span>
            {isRunning ? (
              <button type="button" className="btn btn-stop" onClick={handleStop}>
                <Square size={14} strokeWidth={2} aria-hidden="true" />
                Parar
              </button>
            ) : (
              <button
                type="button"
                className="btn btn-run"
                onClick={handleRun}
                disabled={isSubmitting}
              >
                <Play size={14} strokeWidth={2} aria-hidden="true" />
                Executar
              </button>
            )}
            <button
              type="button"
              className="btn btn-submit"
              onClick={handleSubmit}
              disabled={isRunning || isSubmitting}
            >
              {isSubmitting ? (
                <span className="btn-spinner" aria-hidden="true" />
              ) : (
                <Send size={14} strokeWidth={2} aria-hidden="true" />
              )}
              {isSubmitting ? "Avaliando…" : "Submeter"}
            </button>
            <button
              type="button"
              className={`btn btn-reset ${confirmingReset ? "confirming" : ""}`}
              onClick={handleReset}
              onBlur={() => confirmingReset && setConfirmingReset(false)}
            >
              <RotateCcw size={14} strokeWidth={2} aria-hidden="true" />
              {confirmingReset ? "Apagar meu código?" : "Restaurar código"}
            </button>
            <span className="visually-hidden" aria-live="polite">
              {confirmingReset
                ? "Clique de novo para apagar seu código e voltar ao código inicial."
                : ""}
            </span>
          </div>

          <div className="code-area">
            <CodeEditor code={code} onChange={handleCodeChange} />
          </div>

          {module.sampleInput && (
            <div className="stdin-area">
              <label className="stdin-label" htmlFor="stdin-input">
                Entrada do programa
                <span className="stdin-hint">
                  o que o <code translate="no">input()</code> vai ler, uma linha por chamada
                </span>
              </label>
              <textarea
                id="stdin-input"
                name="stdin"
                className="stdin-textarea"
                value={stdinInput}
                onChange={(e) => setStdinInput(e.target.value)}
                rows={Math.min(stdinInput.split("\n").length + 1, 6)}
                spellCheck={false}
                autoComplete="off"
              />
            </div>
          )}

          <div className="terminal-area">
            {submissionResult ? (
              <SubmissionFeedback result={submissionResult} />
            ) : (
              <TerminalOutput
                output={output}
                error={error}
                isRunning={isRunning}
                executionTimeMs={executionTimeMs}
              />
            )}
          </div>
        </section>
      </div>
    </div>
  );
}
