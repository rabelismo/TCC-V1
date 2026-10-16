import { useRef, useState } from "react";
import { AlertCircle } from "lucide-react";
import { login, register } from "../services/api";
import type { AuthUser } from "../types";

interface AuthPageProps {
  onAuth: (user: AuthUser) => void;
}

const BOARD = ["X", "O", "", "", "X", "O", "", "", "X"];

export default function AuthPage({ onAuth }: AuthPageProps) {
  const [isLogin, setIsLogin] = useState(true);
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const errorRef = useRef<HTMLDivElement>(null);

  const switchMode = (login: boolean) => {
    setIsLogin(login);
    setError("");
  };

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
        setError(
          axiosErr.response?.data?.error ||
            (isLogin
              ? "Email ou senha incorretos. Confira os dados e tente de novo."
              : "Não foi possível criar a conta. Confira os dados e tente de novo.")
        );
      } else {
        setError("Sem conexão com o servidor. Tente de novo em alguns segundos.");
      }
      requestAnimationFrame(() => errorRef.current?.focus());
    } finally {
      setLoading(false);
    }
  };

  const panelId = "auth-panel";

  return (
    <div className="auth-page">
      <section className="auth-intro" aria-labelledby="auth-headline">
        <p className="auth-eyebrow" translate="no">GDBL · Python</p>
        <h1 id="auth-headline" className="auth-headline">
          Aprenda Python construindo um jogo da velha.
        </h1>
        <p className="auth-lede">
          São 14 módulos curtos. Cada um ensina um conceito e acrescenta uma peça
          ao jogo, até ele estar completo e rodando no seu navegador.
        </p>
        <div className="auth-preview" aria-hidden="true">
          <div className="auth-board">
            {BOARD.map((cell, i) => (
              <span key={i} className={cell === "X" ? "x" : undefined}>
                {cell}
              </span>
            ))}
          </div>
          <div className="auth-code" translate="no">
            <span className="kw">def</span> venceu(tab, j):{"\n"}
            {"  "}<span className="kw">return</span> all({"\n"}
            {"    "}tab[i][i] == j{"\n"}
            {"    "}<span className="kw">for</span> i <span className="kw">in</span> range(3)){"\n"}
            {"\n"}
            venceu(tab, <span className="str">"X"</span>)  <span># True</span>
          </div>
        </div>
      </section>

      <main className="auth-panel">
        <div className="auth-card">
          <h2 className="auth-title">{isLogin ? "Entrar na sua conta" : "Criar sua conta"}</h2>
          <p className="auth-subtitle">
            {isLogin
              ? "Seu progresso e seu código ficam salvos."
              : "Leva menos de um minuto."}
          </p>

          <div className="auth-tabs" role="tablist" aria-label="Tipo de acesso">
            <button
              type="button"
              role="tab"
              id="tab-login"
              aria-selected={isLogin}
              aria-controls={panelId}
              className="auth-tab"
              onClick={() => switchMode(true)}
            >
              Entrar
            </button>
            <button
              type="button"
              role="tab"
              id="tab-register"
              aria-selected={!isLogin}
              aria-controls={panelId}
              className="auth-tab"
              onClick={() => switchMode(false)}
            >
              Cadastrar
            </button>
          </div>

          <form
            id={panelId}
            role="tabpanel"
            aria-labelledby={isLogin ? "tab-login" : "tab-register"}
            onSubmit={handleSubmit}
            className="auth-form"
          >
            {!isLogin && (
              <div className="auth-field">
                <label className="auth-label" htmlFor="auth-name">Nome</label>
                <input
                  id="auth-name"
                  name="name"
                  type="text"
                  placeholder="Maria Silva…"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  required
                  className="auth-input"
                  autoComplete="name"
                />
              </div>
            )}
            <div className="auth-field">
              <label className="auth-label" htmlFor="auth-email">Email</label>
              <input
                id="auth-email"
                name="email"
                type="email"
                inputMode="email"
                placeholder="voce@escola.edu.br…"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                className="auth-input"
                autoComplete="email"
                spellCheck={false}
                aria-invalid={error ? true : undefined}
                aria-describedby={error ? "auth-error" : undefined}
              />
            </div>
            <div className="auth-field">
              <label className="auth-label" htmlFor="auth-password">Senha</label>
              <input
                id="auth-password"
                name="password"
                type="password"
                placeholder={isLogin ? "Sua senha…" : "Mínimo de 6 caracteres…"}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                minLength={6}
                className="auth-input"
                autoComplete={isLogin ? "current-password" : "new-password"}
                aria-invalid={error ? true : undefined}
                aria-describedby={error ? "auth-error" : undefined}
              />
            </div>

            {error && (
              <div
                id="auth-error"
                ref={errorRef}
                className="auth-error"
                role="alert"
                tabIndex={-1}
              >
                <AlertCircle size={16} strokeWidth={1.75} aria-hidden="true" />
                <span>{error}</span>
              </div>
            )}

            <button type="submit" className="btn btn-submit auth-submit" disabled={loading}>
              {loading && <span className="btn-spinner" aria-hidden="true" />}
              {loading
                ? isLogin
                  ? "Entrando…"
                  : "Criando conta…"
                : isLogin
                  ? "Entrar"
                  : "Criar conta"}
            </button>
          </form>

          <p className="auth-footnote">
            {isLogin ? "Ainda não tem conta? " : "Já tem conta? "}
            <button
              type="button"
              className="auth-link"
              onClick={() => switchMode(!isLogin)}
            >
              {isLogin ? "Cadastre-se" : "Entre"}
            </button>
          </p>
        </div>
      </main>
    </div>
  );
}
