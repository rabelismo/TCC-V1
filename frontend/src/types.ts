export interface Module {
  id: number;
  orderIndex: number;
  title: string;
  description: string;
  theoryMarkdown: string;
  starterCode: string;
  expectedOutput: string;
  concept: string;
  sampleInput?: string;
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
  module: { id: number; title: string; orderIndex: number };
  status: "NOT_STARTED" | "IN_PROGRESS" | "COMPLETED";
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

export interface AuthResponse {
  token: string;
  userId: number;
  name: string;
  email: string;
  role: string;
}

export interface AuthUser {
  token: string;
  userId: number;
  name: string;
  email: string;
  role: string;
}
