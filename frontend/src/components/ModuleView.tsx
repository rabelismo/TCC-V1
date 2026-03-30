import { useState } from "react";
import { Play, RotateCcw, Send } from "lucide-react";
import TheoryPanel from "./TheoryPanel";
import CodeEditor from "./CodeEditor";
import TerminalOutput from "./TerminalOutput";
import SubmissionFeedback from "./SubmissionFeedback";
import { executeCode, submitCode } from "../services/api";
import type { Module, SubmissionResult } from "../types";

interface ModuleViewProps {
  module: Module;
}

export default function ModuleView({ module }: ModuleViewProps) {
  const [code, setCode] = useState(module.starterCode);
  const [output, setOutput] = useState("");
  const [error, setError] = useState("");
  const [isRunning, setIsRunning] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [executionTimeMs, setExecutionTimeMs] = useState<number | undefined>();
  const [submissionResult, setSubmissionResult] = useState<SubmissionResult | null>(null);

  const handleRun = async () => {
    setIsRunning(true);
    setOutput("");
    setError("");
    setExecutionTimeMs(undefined);
    setSubmissionResult(null);

    try {
      const result = await executeCode({ code });
      setOutput(result.output);
      setError(result.error);
      setExecutionTimeMs(result.executionTimeMs);
    } catch {
      setError("Erro de conexão com o servidor. Verifique se o backend está rodando.");
    } finally {
      setIsRunning(false);
    }
  };

  const handleSubmit = async () => {
    setIsSubmitting(true);
    setSubmissionResult(null);

    try {
      const result = await submitCode({ moduleId: module.id, code });
      setSubmissionResult(result);
    } catch {
      setError("Erro ao submeter. Verifique se o backend está rodando.");
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleReset = () => {
    setCode(module.starterCode);
    setOutput("");
    setError("");
    setExecutionTimeMs(undefined);
    setSubmissionResult(null);
  };

  return (
    <div className="module-view">
      <div className="module-header">
        <div>
          <h1 className="module-title">{module.title}</h1>
          <p className="module-description">{module.description}</p>
        </div>
      </div>

      <div className="module-content">
        <div className="panel-theory">
          <TheoryPanel markdown={module.theoryMarkdown} />
        </div>

        <div className="panel-code">
          <div className="code-toolbar">
            <button className="btn btn-run" onClick={handleRun} disabled={isRunning || isSubmitting}>
              <Play size={16} />
              {isRunning ? "Executando..." : "Executar"}
            </button>
            <button className="btn btn-submit" onClick={handleSubmit} disabled={isRunning || isSubmitting}>
              <Send size={16} />
              {isSubmitting ? "Avaliando..." : "Submeter"}
            </button>
            <button className="btn btn-reset" onClick={handleReset}>
              <RotateCcw size={16} />
              Resetar
            </button>
          </div>

          <div className="code-area">
            <CodeEditor code={code} onChange={setCode} />
          </div>

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
        </div>
      </div>
    </div>
  );
}
