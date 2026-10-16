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
| B1 | Execução normal de programas pequenos | sem `setrlimit` em `sandbox_wrapper.py` nem no `ProcessBuilder` | `sandbox_wrapper.py` (limites antes de remover builtins) | — | teste Linux-only em `PythonExecutionServiceTest`; **verificação real exige Linux/Docker** | Done — teste Linux-only pulado no Windows; validar no container |
| B2 | Programas rodam com UTF-8 e sem bytecode | `PythonExecutionService` copia o ambiente do JVM (`pb.environment()`) | `PythonExecutionService.buildProcess` | — | teste do ambiente do `ProcessBuilder` | Done |
| B3 | Execução falha visível ao aluno quando há erro no código | `tryAcquire()` falho devolve `exitCode 1` e reprova critério | `ExecutionCapacityException` → 503 no `GlobalExceptionHandler` | — | testes de serviço + `SubmissionServiceTest` | Done |
| B4 | Critérios avaliados na ordem; saída compartilhada entre critérios sem input | 4 execuções × 10s > 30s | orçamento total em `SubmissionService` | — | teste de orçamento esgotado | Done |
| B5 | Bloqueio das funções perigosas | `open\s*\(` casa `reopen(`; `import math` dá erro críptico | `PythonExecutionService.validateCode` | — | testes de validação | Done |
| D1 | Esquema atual do banco | prod: Flyway + `ddl-auto=update`, **mas o Flyway nunca rodou**: faltava `spring-boot-starter-flyway` no Boot 4 (nenhum log de migração; `./mvnw dependency:tree \| grep flyway` sem `spring-boot-flyway`) | Flyway (único dono), `ddl-auto=validate` | `contextLoads` contra Postgres local | `./mvnw test` | Done — validado em banco existente e em banco vazio temporário |
| D2 | Sincronização de módulos por `orderIndex`, progresso preservado | JSON inválido é só logado; módulo fica sem critérios | `ModuleCatalogSync` (transacional, falha rápido) | `contextLoads` | teste com JSON inválido | Done |
| H1 | 30 req/min em `/api/execute` e `/api/submit` | chave = 1º valor de `X-Forwarded-For` (falsificável); mapa nunca limpo | `RateLimitFilter` por usuário autenticado | — | testes do filtro | Done |
| H2 | CORS para as origens configuradas | CORS em `WebConfig` e `SecurityConfig` | `SecurityConfig` | — | preflight real com a app rodando | Done |
| H3 | — (endpoint removido) | `POST /api/modules` recebe entidade JPA; ninguém chama | conteúdo vem só do JSON | matcher em `SecurityConfig` | `./mvnw test` | Done |
| H4 | Autenticação por JWT | `JwtAuthFilter` faz parse 2× (`isValid` + `parseToken`); 3 métodos mortos | `JwtService.verify` | — | testes de `JwtService` | Done |
| H5 | Erros de requisição devolvem 4xx | JSON malformado virava 500 (achado no teste real); corpo do 429 saía em ISO-8859-1 | `GlobalExceptionHandler` / `RateLimitFilter` | — | testes + smoke contra a app rodando | Done |
| B6 | Execução que funciona devolve saída e exit code | `catch (Exception)` em `PythonExecutionService` devolvia `exitCode 1` → falha de infraestrutura reprovava o aluno e contava tentativa (2ª passada) | `PythonExecutionService` lança exceção → 500 | — | teste com interpretador inexistente | Done |
| F1 | 401 em rota protegida encerra a sessão e recarrega | login com senha errada recarrega a página (interceptor 401) | interceptor de `api.ts` ignora `/auth/*` | — | `src/test/api.test.ts` | Done |
| F2 | — | interceptor repete POST em 5xx (submissão contada 3×) | frontend `api.ts` | — | — | Proposed (fora do escopo: frontend) |
| P1 | — | `expectedOutput` do módulo é exposto no `GET /api/modules` (= resposta de um critério em 5 módulos) | decisão de produto | testes do frontend usam o campo | — | Proposed (precisa de decisão) |
| E1 | — | `AuthController` devolve `{"error": mensagem}` à mão; o `GlobalExceptionHandler` devolve `{status, error: motivo HTTP, message}`; o frontend lê `data.error` | contrato único de erro, junto com F1 | `AuthControllerTest`, `AuthPage.tsx` | — | Proposed (exige mudar backend e frontend juntos) |
| P2 | — | critério oculto reprovado devolve `actualOutput` (saída para a entrada oculta); o frontend não exibe | decisão de produto | — | — | Proposed |
| X1 | Recriar critérios a cada boot | IDs de critério mudam a cada deploy | — | — | — | Deliberate — do not fix: ninguém persiste `criterionId` |
| X2 | `sys.modules[...] = None` no wrapper | redundante com a remoção de `__import__` | — | — | — | Deliberate — do not fix: defesa em profundidade |
| X3 | `@JsonIgnoreProperties` nas entidades | entidades não são mais serializadas | — | — | — | Rejected: churn sem ganho |
| X4 | JWT no `localStorage`, semáforo e rate limit em memória | — | — | — | — | Deliberate — do not fix: aceitável com uma instância e markdown sem HTML bruto |

## Passadas

1. Auditoria inicial (arquitetura + taxonomia) → B1–B5, D1–D2, H1–H4.
2. Segunda passada após T1–T3 (`grep` de `catch (Exception`, `ResponseEntity<?>`, métodos de repositório sem uso) → B6, E1, P2. Teste com a app rodando → H5.
3. Terceira passada: nenhum novo defeito no backend. Ledger seco.

## Lições para a próxima auditoria

- **Spring Boot 4 modularizou a autoconfiguração**: `flyway-core` sozinho não executa migrations; é preciso `spring-boot-starter-flyway`. Conferir sempre pelos logs (`Migrating schema`), nunca pela propriedade `spring.flyway.enabled`.
- O teste com a app rodando achou dois defeitos (JSON malformado → 500, 429 sem charset) que nenhum teste unitário pegava.
- No Git Bash do Windows, acentos em `curl -d` saem fora de UTF-8; usar Python/arquivo para testar corpos com acento.
- `contextLoads` roda contra o Postgres local de desenvolvimento e o altera (sincroniza módulos; agora também aplica migrations).
