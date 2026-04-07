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
  user: { id: number };
  module: { id: number };
  status: "NOT_STARTED" | "IN_PROGRESS" | "COMPLETED";
  lastCodeSnapshot: string | null;
  attempts: number;
  startedAt: string | null;
  completedAt: string | null;
}

export interface SubmissionRequest {
  moduleId: number;
  userId?: number;
  code: string;
}

export interface CriterionResult {
  criterionId: number;
  description: string;
  passed: boolean;
  hint: string | null;
  actualOutput: string;
  hidden: boolean;
}

export interface SubmissionResult {
  allPassed: boolean;
  passedCount: number;
  totalCount: number;
  criteria: CriterionResult[];
}
