interface TerminalOutputProps {
  output: string;
  error: string;
  isRunning: boolean;
  executionTimeMs?: number;
}

const msFormat = new Intl.NumberFormat("pt-BR");

export default function TerminalOutput({
  output,
  error,
  isRunning,
  executionTimeMs,
}: TerminalOutputProps) {
  const dotState = isRunning ? "running" : error ? "error" : output ? "ok" : "";
  const status = isRunning
    ? "Executando…"
    : error
      ? "Execução terminou com erro."
      : output
        ? "Execução concluída."
        : "";

  return (
    <div className="terminal-output" role="region" aria-label="Saída do programa">
      <div className="terminal-header">
        <span className="terminal-title">
          <span className={`terminal-dot ${dotState}`} aria-hidden="true" />
          Saída
        </span>
        {executionTimeMs !== undefined && executionTimeMs > 0 && (
          <span className="terminal-time">
            {msFormat.format(executionTimeMs)}&nbsp;ms
          </span>
        )}
      </div>
      <span className="visually-hidden" role="status">
        {status}
      </span>
      <pre className="terminal-body">
        {isRunning && <span className="terminal-running">Executando…</span>}
        {output && <span className="terminal-stdout">{output}</span>}
        {error && <span className="terminal-stderr">{error}</span>}
        {!isRunning && !output && !error && (
          <span className="terminal-placeholder">
            Clique em “Executar” para ver a saída do seu código aqui.
          </span>
        )}
      </pre>
    </div>
  );
}
