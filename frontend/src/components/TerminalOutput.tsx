interface TerminalOutputProps {
  output: string;
  error: string;
  isRunning: boolean;
  executionTimeMs?: number;
}

export default function TerminalOutput({
  output,
  error,
  isRunning,
  executionTimeMs,
}: TerminalOutputProps) {
  return (
    <div className="terminal-output" role="region" aria-label="Saída do programa">
      <div className="terminal-header">
        <span className="terminal-title">Terminal</span>
        {executionTimeMs !== undefined && executionTimeMs > 0 && (
          <span className="terminal-time">{executionTimeMs}ms</span>
        )}
      </div>
      <pre className="terminal-body" aria-live="polite" aria-atomic="false">
        {isRunning && (
          <span className="terminal-running" role="status">Executando...</span>
        )}
        {output && <span className="terminal-stdout">{output}</span>}
        {error && <span className="terminal-stderr" role="alert">{error}</span>}
        {!isRunning && !output && !error && (
          <span className="terminal-placeholder">
            Clique em "Executar" para ver a saída do seu código aqui.
          </span>
        )}
      </pre>
    </div>
  );
}
