export interface Module {
  id: number;
  orderIndex: number;
  title: string;
  description: string;
  theoryMarkdown: string;
  starterCode: string;
  expectedOutput: string;
  concept: string;
}

export interface CodeExecutionRequest {
  code: string;
  input?: string;
}

export interface CodeExecutionResponse {
  output: string;
  error: string;
  exitCode: number;
  executionTimeMs: number;
}

export interface StudentProgress {
  id: number;
  userId: number;
  moduleId: number;
  status: "NOT_STARTED" | "IN_PROGRESS" | "COMPLETED";
  lastCodeSnapshot: string | null;
  attempts: number;
  startedAt: string | null;
  completedAt: string | null;
}
