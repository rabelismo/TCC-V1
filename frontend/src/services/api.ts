import axios from "axios";
import type {
  Module,
  CodeExecutionRequest,
  CodeExecutionResponse,
  SubmissionRequest,
  SubmissionResult,
} from "../types";

const api = axios.create({
  baseURL: "http://localhost:8080/api",
  headers: { "Content-Type": "application/json" },
});

export async function fetchModules(): Promise<Module[]> {
  const { data } = await api.get<Module[]>("/modules");
  return data;
}

export async function fetchModule(id: number): Promise<Module> {
  const { data } = await api.get<Module>(`/modules/${id}`);
  return data;
}

export async function executeCode(
  request: CodeExecutionRequest
): Promise<CodeExecutionResponse> {
  const { data } = await api.post<CodeExecutionResponse>("/execute", request);
  return data;
}

export async function submitCode(
  request: SubmissionRequest
): Promise<SubmissionResult> {
  const { data } = await api.post<SubmissionResult>("/submit", request);
  return data;
}
