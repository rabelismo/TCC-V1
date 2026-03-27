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
    <div className="terminal-output">
      <div className="terminal-header">
        <span className="terminal-title">Terminal</span>
        {executionTimeMs !== undefined && executionTimeMs > 0 && (
          <span className="terminal-time">{executionTimeMs}ms</span>
        )}
      </div>
      <pre className="terminal-body">
        {isRunning && (
          <span className="terminal-running">Executando...</span>
        )}
        {output && <span className="terminal-stdout">{output}</span>}
        {error && <span className="terminal-stderr">{error}</span>}
        {!isRunning && !output && !error && (
          <span className="terminal-placeholder">
            Clique em "Executar" para ver a saída do seu código aqui.
          </span>
        )}
      </pre>
    </div>
  );
}
