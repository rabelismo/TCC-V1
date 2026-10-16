import { AxiosError, type AxiosResponse, type InternalAxiosRequestConfig } from "axios";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { api } from "../services/api";

const reload = vi.fn();
const originalLocation = window.location;
const originalAdapter = api.defaults.adapter;

function respondWith(status: number, data: unknown = {}) {
  const adapter = vi.fn(async (config: InternalAxiosRequestConfig) => {
    const response = { status, statusText: "", data, headers: {}, config } as AxiosResponse;
    if (status >= 400) {
      throw new AxiosError("erro", "ERR_BAD_RESPONSE", config, null, response);
    }
    return response;
  });
  api.defaults.adapter = adapter;
  return adapter;
}

async function settle<T>(request: Promise<T>) {
  const outcome = request.then(
    (value) => ({ value, error: undefined }),
    (error: AxiosError) => ({ value: undefined, error })
  );
  await vi.runAllTimersAsync();
  return outcome;
}

beforeEach(() => {
  vi.useFakeTimers();
  reload.mockReset();
  Object.defineProperty(window, "location", {
    configurable: true,
    value: { ...originalLocation, reload },
  });
  localStorage.setItem("tcc_auth", JSON.stringify({ token: "t" }));
});

afterEach(() => {
  vi.useRealTimers();
  api.defaults.adapter = originalAdapter;
  Object.defineProperty(window, "location", { configurable: true, value: originalLocation });
  localStorage.clear();
});

describe("interceptor de 401", () => {
  it("não recarrega a página quando o login falha", async () => {
    respondWith(401, { message: "Email ou senha inválidos" });

    const { error } = await settle(api.post("/auth/login", {}));

    expect(error?.response?.status).toBe(401);
    expect(reload).not.toHaveBeenCalled();
    expect(localStorage.getItem("tcc_auth")).not.toBeNull();
  });

  it("encerra a sessão quando uma rota protegida devolve 401", async () => {
    respondWith(401);

    await settle(api.get("/modules"));

    expect(reload).toHaveBeenCalledOnce();
    expect(localStorage.getItem("tcc_auth")).toBeNull();
  });
});
