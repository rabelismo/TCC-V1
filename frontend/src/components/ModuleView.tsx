import { useState } from "react";
import { Play, RotateCcw } from "lucide-react";
import TheoryPanel from "./TheoryPanel";
import CodeEditor from "./CodeEditor";
import TerminalOutput from "./TerminalOutput";
import { executeCode } from "../services/api";
import type { Module } from "../types";

interface ModuleViewProps {
  module: Module;
}

export default function ModuleView({ module }: ModuleViewProps) {
  const [code, setCode] = useState(module.starterCode);
  const [output, setOutput] = useState("");
  const [error, setError] = useState("");
  const [isRunning, setIsRunning] = useState(false);
  const [executionTimeMs, setExecutionTimeMs] = useState<number | undefined>();

  const handleRun = async () => {
    setIsRunning(true);
    setOutput("");
    setError("");
    setExecutionTimeMs(undefined);

    try {
      const result = await executeCode({ code });
      setOutput(result.output);
      setError(result.error);
      setExecutionTimeMs(result.executionTimeMs);
    } catch (err) {
      setError("Erro de conexão com o servidor. Verifique se o backend está rodando.");
    } finally {
      setIsRunning(false);
    }
  };

  const handleReset = () => {
    setCode(module.starterCode);
    setOutput("");
    setError("");
    setExecutionTimeMs(undefined);
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
            <button className="btn btn-run" onClick={handleRun} disabled={isRunning}>
              <Play size={16} />
              {isRunning ? "Executando..." : "Executar"}
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
            <TerminalOutput
              output={output}
              error={error}
              isRunning={isRunning}
              executionTimeMs={executionTimeMs}
            />
          </div>
        </div>
      </div>
    </div>
  );
}
