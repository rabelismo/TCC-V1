# TCC-V1 - Plataforma Educacional GDBL

Plataforma web interativa para ensino de logica de programacao para calouros, baseada em:

- **GDBL (Game Development-Based Learning)**
- **Constructionism (Seymour Papert)**

O aluno aprende construindo um jogo real passo a passo: **Jogo da Velha em Python**.

---

## Visao Geral

O curriculo e dividido em micro-modulos, cada um focado em um conceito:

1. Variaveis e `print`
2. Arrays 2D (tabuleiro)
3. `if/else` (validacao de jogadas)
4. `while` loops (motor do jogo)

A plataforma oferece:

- teoria em Markdown
- editor de codigo Python no navegador (Monaco)
- execucao de codigo via backend Java
- terminal com saida de execucao
- persistencia de modulos e progresso do aluno

---

## Arquitetura

Estrutura em monorepo:

```text
TCC-V1/
├── backend/   # Spring Boot + PostgreSQL
└── frontend/  # React + Vite + Monaco
```

### Backend (`backend/`)

- **Java 21**
- **Spring Boot 4**
- **Spring Web MVC**
- **Spring Data JPA**
- **PostgreSQL**
- **Lombok**

Responsabilidades:

- API REST de modulos e progresso
- execucao de codigo Python (com validacoes basicas de seguranca)
- inicializacao de dados de modulos

### Frontend (`frontend/`)

- **React + TypeScript**
- **Vite**
- **Monaco Editor**
- **React Markdown**
- **Axios**

Responsabilidades:

- renderizar teoria (Markdown)
- editor de codigo
- execucao e exibicao de output/erros
- navegacao entre modulos

---

## Modelo de Dados (resumo)

Entidades principais:

- `User`
- `Module`
- `StudentProgress`

Relacionamentos:

- `User` 1:N `StudentProgress`
- `Module` 1:N `StudentProgress`

`StudentProgress` possui restricao unica para `(user_id, module_id)`.

---

## Endpoints Principais

Base URL: `http://localhost:8080/api`

- `GET /modules` - lista modulos ordenados
- `GET /modules/{id}` - detalhes de um modulo
- `POST /modules` - cria modulo
- `POST /execute` - executa codigo Python
- `GET /progress/user/{userId}` - progresso por usuario
- `POST /progress` - atualiza progresso

---

## Como Rodar o Projeto

## 1) Pre-requisitos

- Java 21
- PostgreSQL
- Node.js 20+ e npm
- (Opcional) nvm para gerenciar versoes do Node

## 2) Banco de dados

Crie o banco:

```sql
CREATE DATABASE tcc_gdbl;
```

Configuracao atual em `backend/src/main/resources/application.properties`:

- URL: `jdbc:postgresql://localhost:5432/tcc_gdbl`
- usuario: `postgres`
- senha: `postgres`

Se necessario, ajuste para seu ambiente.

## 3) Rodar backend

No Windows (PowerShell):

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

No Linux/macOS:

```bash
cd backend
./mvnw spring-boot:run
```

Backend sobe em `http://localhost:8080`.

## 4) Rodar frontend

```powershell
cd frontend
npm install
npm run dev
```

Frontend sobe em `http://localhost:5173`.

---

## Seguranca na Execucao de Python

O backend aplica protecoes basicas:

- timeout de execucao
- bloqueio de imports sensiveis (`os`, `subprocess`, etc.)
- bloqueio de funcoes perigosas (`exec`, `eval`, `open`, etc.)
- limite de tamanho de codigo

> Observacao: para ambiente de producao, o ideal e executar codigo em **sandbox isolado** (container/jail por execucao).

---

## Scripts Uteis

### Frontend

- `npm run dev` - modo desenvolvimento
- `npm run build` - build de producao
- `npm run preview` - preview da build

### Backend

- `.\mvnw.cmd spring-boot:run` - sobe aplicacao
- `.\mvnw.cmd test` - executa testes

---

## Roadmap Sugerido

- autenticacao/autorizacao (JWT ou sessao)
- avaliacao automatica por modulo (testes de corretude)
- salvamento automatico de snapshots de codigo
- dashboard de progresso do aluno
- execucao de codigo em container isolado
- deploy (Docker + CI/CD)

---

## Licenca

Consulte o arquivo `LICENSE`.