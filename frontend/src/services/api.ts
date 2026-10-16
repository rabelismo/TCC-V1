import axios from "axios";
import type {
  Module,
  CodeExecutionRequest,
  CodeExecutionResponse,
  SubmissionRequest,
  SubmissionResult,
  StudentProgress,
  AuthResponse,
} from "../types";

const MAX_RETRIES = 2;
const RETRY_DELAY_MS = 1000;

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "http://localhost:8080/api",
  headers: { "Content-Type": "application/json" },
  timeout: 30_000,
});

function isRetryable(method?: string, url?: string, status?: number): boolean {
  if (status === undefined) return false;
  if (method?.toLowerCase() === "get") return status >= 500;
  return method?.toLowerCase() === "post" && url === "/execute" && status === 503;
}

api.interceptors.request.use((config) => {
  const stored = localStorage.getItem("tcc_auth");
  if (stored) {
    try {
      const { token } = JSON.parse(stored);
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    } catch { /* invalid json */ }
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401 && !error.config?.url?.startsWith("/auth/")) {
      localStorage.removeItem("tcc_auth");
      window.location.reload();
      return Promise.reject(error);
    }

    const config = error.config;
    if (config && isRetryable(config.method, config.url, error.response?.status)) {
      config.__retryCount = (config.__retryCount ?? 0) + 1;
      if (config.__retryCount > MAX_RETRIES) {
        return Promise.reject(error);
      }
      await new Promise((r) => setTimeout(r, RETRY_DELAY_MS * config.__retryCount));
      return api(config);
    }

    return Promise.reject(error);
  }
);

export async function login(email: string, password: string): Promise<AuthResponse> {
  const { data } = await api.post<AuthResponse>("/auth/login", { email, password });
  return data;
}

export async function register(name: string, email: string, password: string): Promise<AuthResponse> {
  const { data } = await api.post<AuthResponse>("/auth/register", { name, email, password });
  return data;
}

export async function fetchModules(): Promise<Module[]> {
  const { data } = await api.get<Module[]>("/modules");
  return data;
}

export async function executeCode(
  request: CodeExecutionRequest,
  signal?: AbortSignal
): Promise<CodeExecutionResponse> {
  const { data } = await api.post<CodeExecutionResponse>("/execute", request, {
    signal,
  });
  return data;
}

export async function submitCode(
  request: SubmissionRequest
): Promise<SubmissionResult> {
  const { data } = await api.post<SubmissionResult>("/submit", request);
  return data;
}

export async function fetchUserProgress(
  userId: number
): Promise<StudentProgress[]> {
  const { data } = await api.get<StudentProgress[]>(
    `/progress/user/${userId}`
  );
  return data;
}
