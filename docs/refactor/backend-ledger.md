# Refatoração do backend — relatório e ledger

Auditoria feita em `9ecc445` (2026-10-15), a partir da avaliação de arquitetura da mesma sessão.
Branch do trem: `refactor/backend-hardening`, um commit por ticket.

## Veredito de arquitetura

SPA React + monólito Spring Boot em camadas + Postgres + conteúdo em JSON versionado é o formato
certo para o problema (uma instância, Render free). Nenhuma mudança estrutural é proposta.
A dívida está em três fronteiras: **execução de código do aluno** (mesmo container da API, sem
limites, herda segredos), **contratos de erro** (falta de capacidade vira "código errado") e
**posse do esquema** (Flyway e Hibernate `update` ao mesmo tempo).

## Medições (re-derive antes de usar)

| Medida | Valor em `9ecc445` | Comando |
|---|---|---|
| Execuções Python por submissão, por módulo | máx. 4 (module-14); 4 módulos com ≥3 | script em `docs/refactor/measure-runs.py` — `python docs/refactor/measure-runs.py` |
| Pior caso de tempo de uma submissão | 4 × 10s = 40s > 30s (axios e `proxy_read_timeout`) | idem + `python.execution.timeout-seconds` |
| `import` dentro do sandbox | sempre falha: `ImportError: __import__ not found` | `(cat backend/src/main/resources/sandbox_wrapper.py; echo 'import math') > t.py && python -B -S t.py` |
| Módulos cujo `expectedOutput` aparece em `GET /api/modules` e é igual ao de um critério | 5 | `python docs/refactor/measure-runs.py` |
| Símbolos sem uso no código de produção | `JwtService.getUserId`, `ModuleRepository.findByConcept`, `UserRepository.existsByEmail` (1 ocorrência cada = a declaração) | `grep -rn "<símbolo>" backend/src` |

## Pins encontrados

- `AuthControllerTest` faz stub de `userRepo.findByEmail(...)` no cadastro → o `AuthController` precisa continuar usando `findByEmail` (não trocar por `existsByEmail`).
- `TccGrApplicationTests.contextLoads` sobe o contexto inteiro contra o Postgres local (`localhost:5432`) e roda o `DataInitializer` → qualquer mudança de esquema/validação é verificada por ele.
- `SubmissionServiceTest` usa `new PythonExecutionService` só como mock → mudar o construtor é livre.
- Nenhum teste referencia `PythonExecutionService`, `RateLimitFilter`, `WebConfig`, `DataInitializer`, `JwtAuthFilter`.

## Ledger

| ID | Comportamento preservado | Evidência | Dono após a mudança | Pins | Validação | Status |
|---|---|---|---|---|---|---|
| T0 | Progresso só muda por submissão avaliada; identidade vem do token | revisão de design anterior | `StudentProgress` / `StudentProgressService.recordSubmission` | — | `./mvnw test` | Done |
| B1 | Execução normal de programas pequenos | sem `setrlimit` em `sandbox_wrapper.py` nem no `ProcessBuilder` | `sandbox_wrapper.py` (limites antes de remover builtins) | — | teste Linux-only em `PythonExecutionServiceTest`; **verificação real exige Linux/Docker** | Approved |
| B2 | Programas rodam com UTF-8 e sem bytecode | `PythonExecutionService` copia o ambiente do JVM (`pb.environment()`) | `PythonExecutionService.buildProcess` | — | teste do ambiente do `ProcessBuilder` | Approved |
| B3 | Execução falha visível ao aluno quando há erro no código | `tryAcquire()` falho devolve `exitCode 1` e reprova critério | `ExecutionCapacityException` → 503 no `GlobalExceptionHandler` | — | testes de serviço + `SubmissionServiceTest` | Approved |
| B4 | Critérios avaliados na ordem; saída compartilhada entre critérios sem input | 4 execuções × 10s > 30s | orçamento total em `SubmissionService` | — | teste de orçamento esgotado | Approved |
| B5 | Bloqueio das funções perigosas | `open\s*\(` casa `reopen(`; `import math` dá erro críptico | `PythonExecutionService.validateCode` | — | testes de validação | Approved |
| D1 | Esquema atual do banco | prod: Flyway + `ddl-auto=update`; dev: Flyway desligado | Flyway (único dono), `ddl-auto=validate` | `contextLoads` contra Postgres local | `./mvnw test` | Approved |
| D2 | Sincronização de módulos por `orderIndex`, progresso preservado | JSON inválido é só logado; módulo fica sem critérios | `ModuleCatalogSync` (transacional, falha rápido) | `contextLoads` | teste com JSON inválido | Approved |
| H1 | 30 req/min em `/api/execute` e `/api/submit` | chave = 1º valor de `X-Forwarded-For` (falsificável); mapa nunca limpo | `RateLimitFilter` por usuário autenticado | — | testes do filtro | Approved |
| H2 | CORS para as origens configuradas | CORS em `WebConfig` e `SecurityConfig` | `SecurityConfig` | — | preflight real com a app rodando | Approved |
| H3 | — (endpoint removido) | `POST /api/modules` recebe entidade JPA; ninguém chama | conteúdo vem só do JSON | matcher em `SecurityConfig` | `./mvnw test` | Approved |
| H4 | Autenticação por JWT | `JwtAuthFilter` faz parse 2× (`isValid` + `parseToken`); 3 métodos mortos | `JwtService.verify` | — | testes de `JwtService` | Approved |
| F1 | — | login com senha errada recarrega a página (interceptor 401) | frontend `api.ts` | — | — | Proposed (fora do escopo: frontend) |
| F2 | — | interceptor repete POST em 5xx (submissão contada 3×) | frontend `api.ts` | — | — | Proposed (fora do escopo: frontend) |
| P1 | — | `expectedOutput` do módulo é exposto no `GET /api/modules` (= resposta de um critério em 5 módulos) | decisão de produto | testes do frontend usam o campo | — | Proposed (precisa de decisão) |
| X1 | Recriar critérios a cada boot | IDs de critério mudam a cada deploy | — | — | — | Deliberate — do not fix: ninguém persiste `criterionId` |
| X2 | `sys.modules[...] = None` no wrapper | redundante com a remoção de `__import__` | — | — | — | Deliberate — do not fix: defesa em profundidade |
| X3 | `@JsonIgnoreProperties` nas entidades | entidades não são mais serializadas | — | — | — | Rejected: churn sem ganho |
| X4 | JWT no `localStorage`, semáforo e rate limit em memória | — | — | — | — | Deliberate — do not fix: aceitável com uma instância e markdown sem HTML bruto |
