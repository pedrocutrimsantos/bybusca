# ByBusca Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Entregar scaffold executável do ByBusca (API Kotlin/Spring + React/Vite + Postgres FTS + PSE opcional) alinhado à spec e às docs 18.1–18.12.

**Architecture:** Monólito escolar — Search Facade unifica FTS interno (ACL) e Google PSE (web pública); busca aberta na rede; JWT para conteúdo restrito.

**Tech Stack:** JDK 21, Kotlin, Spring Boot 3.3, PostgreSQL 16, Flyway, React 18, Vite 5, TypeScript 5, Docker Compose.

---

### Task 1: Infra e raiz

**Files:**
- Create: `docker-compose.yml`, `.env.example`, `.gitignore`, `README.md`

- [x] Subir Postgres via Compose *(alternativa: Postgres local + `scripts/setup-db.sql`)*
- [x] Documentar comandos no README

### Task 2: API — bootstrap

**Files:**
- Create: `apps/api/settings.gradle.kts`, `build.gradle.kts`, `src/main/kotlin/br/bybusca/*`

- [x] Application + Security JWT + CORS
- [x] Flyway `V1__init.sql` + seed
- [x] Search / Suggest / Auth / Index / Admin endpoints
- [x] PseClient com timeout e degradação
- [x] Health `GET /api/v1/health` + testes unitários ACL

### Task 3: Web — bootstrap

**Files:**
- Create: `apps/web/package.json`, `vite.config.ts`, `src/*`

- [x] Home tipo Google
- [x] Resultados + filtros + warnings
- [x] Login + Admin mínimo
- [x] Autocomplete (suggest) + logout

### Task 4: Verificação

- [x] Postgres local configurado (`bybusca` + Flyway V1–V3)
- [x] API health + search seed (`equacoes`, ACL guest/aluno)
- [x] Web busca demo (Vite em http://127.0.0.1:5173)
