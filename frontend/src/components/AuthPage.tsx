import { useState } from "react";
import { login, register } from "../services/api";
import type { AuthUser } from "../types";

interface AuthPageProps {
  onAuth: (user: AuthUser) => void;
}

export default function AuthPage({ onAuth }: AuthPageProps) {
  const [isLogin, setIsLogin] = useState(true);
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const result = isLogin
        ? await login(email, password)
        : await register(name, email, password);

      const user: AuthUser = {
        token: result.token,
        userId: result.userId,
        name: result.name,
        email: result.email,
        role: result.role,
      };
      localStorage.setItem("tcc_auth", JSON.stringify(user));
      onAuth(user);
    } catch (err: unknown) {
      if (err && typeof err === "object" && "response" in err) {
        const axiosErr = err as { response?: { data?: { error?: string } } };
        setError(axiosErr.response?.data?.error || "Erro ao autenticar.");
      } else {
        setError("Erro de conexão. Verifique se o backend está rodando.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1 className="auth-title">Jogo da Velha — Python</h1>
        <p className="auth-subtitle">Aprenda programação passo a passo</p>

        <div className="auth-tabs">
          <button
            className={`auth-tab ${isLogin ? "active" : ""}`}
            onClick={() => { setIsLogin(true); setError(""); }}
          >
            Entrar
          </button>
          <button
            className={`auth-tab ${!isLogin ? "active" : ""}`}
            onClick={() => { setIsLogin(false); setError(""); }}
          >
            Cadastrar
          </button>
        </div>

        <form onSubmit={handleSubmit} className="auth-form">
          {!isLogin && (
            <input
              type="text"
              placeholder="Nome completo"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              className="auth-input"
              autoComplete="name"
            />
          )}
          <input
            type="email"
            placeholder="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            className="auth-input"
            autoComplete="email"
          />
          <input
            type="password"
            placeholder="Senha"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            minLength={6}
            className="auth-input"
            autoComplete={isLogin ? "current-password" : "new-password"}
          />

          {error && <p className="auth-error">{error}</p>}

          <button type="submit" className="auth-submit" disabled={loading}>
            {loading ? "Aguarde..." : isLogin ? "Entrar" : "Criar conta"}
          </button>
        </form>
      </div>
    </div>
  );
}
