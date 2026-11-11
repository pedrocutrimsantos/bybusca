# ByBusca — Spec de Design (Buscador Próprio)

**Data:** 2026-07-24  
**Etapa:** 8. Buscador Próprio (tarefas 18.1–18.12)  
**Status:** Aprovado em brainstorming  
**Participantes:** Pedro Cutrim · Supervisor: Luis Rodrigo Lima Rodrigues

## 1. Objetivo

Criar o **mecanismo de busca da rede escolar** com experiência tipo Google: indexar conteúdos, acervo, aulas, arquivos, páginas e módulos com **ACL** e **relevância**, aberto na rede da escola, com busca web pública via **Google Programmable Search (PSE)**.

## 2. Decisões fechadas

| Tema | Decisão |
|------|---------|
| Entrega | Documentação completa 18.1–18.12 (Markdown) + plano técnico + scaffold |
| Backend | Kotlin + Spring Boot 3 |
| Frontend | React + Vite + TypeScript |
| Banco | PostgreSQL com Full-Text Search (FTS) + GIN |
| Busca web | Google Programmable Search (somente público) |
| Arquitetura de busca | Índice interno (ACL) no Postgres; PSE complementar para web |
| Abordagem | Monólito escolar (API + SPA + Postgres) |
| Auth MVP | JWT próprio; busca aberta sem login; privado exige token + ACL |
| Deploy | Aberto na rede da escola como mecanismo de busca padrão |
| Formato docs | `docs/18.x-*/README.md` |

## 3. Arquitetura

```text
[Browser na rede da escola]
        │
        ▼
[React + Vite + TS]
        │  REST/JSON
        ▼
[Kotlin Spring Boot 3]
   ├─ Search Facade
   ├─ Auth JWT (opcional)
   ├─ ACL
   ├─ Indexer
   └─ PseClient
        │
        ├─► PostgreSQL (FTS + metadados + ACL + auditoria)
        └─► Google PSE API (web pública)
```

**Princípios**
- Busca aberta por padrão (`public` / `network`).
- Conteúdo restrito só com autenticação + ACL.
- PSE nunca recebe nem indexa material privado.
- Isolamento por `tenant_id` (escola/rede).

## 4. Componentes

### Frontend (`apps/web`)
- Campo único, sugestões, resultados com snippet
- Filtros: tipo, data, disciplina, série/turma
- Visitante vs logado
- Painel admin: reindex, status, config PSE, perfis

### Backend (`apps/api`)
- `GET /api/v1/search`, `GET /api/v1/suggest`
- `POST /api/v1/index/documents` (admin)
- `POST /api/v1/auth/login`
- Serviços: Acl, InternalSearch (FTS), PseSearch, SearchFacade

### Dados
- `tenants`, `users`, `roles`, `permissions`
- `documents`, `document_acl`, `search_audit`
- Coluna FTS + índice GIN

## 5. Fluxos

1. **Visitante:** query sem token → só `public|network` do tenant → FTS → PSE opcional → merge.
2. **Logado:** JWT amplia ACL (turma, disciplina, papel).
3. **Indexação:** upsert idempotente por `external_id` + ACL + `tsvector`.
4. **Sugestões:** prefixo/trigram em títulos permitidos.
5. **Rede:** frontend como URL do mecanismo de busca da escola.

## 6. Erros, segurança e resiliência

- Erros JSON `{ code, message, traceId }` sem vazamento interno.
- Documento sem permissão: **omitido** do resultado (não 403 item a item).
- PSE fora: degradação graciosa (só índice interno).
- Secrets PSE só no backend; rate limit por IP; CORS restrito; auditoria com retenção LGPD.
- Timeout PSE 2–3s + circuit breaker simples.

## 7. Testes e aceite (MVP)

- Unit: ACL, ranking, merge Facade.
- Integração: Testcontainers Postgres.
- API: visitante vs papéis (sem vazamento).
- Frontend: busca, filtros, estados vazio/erro/degradado.

**Aceite mínimo**
1. Busca aberta retorna públicos em ambiente local.
2. Privado nunca aparece sem papel autorizado.
3. Filtro por tipo funciona.
4. Snippet destaca termos.
5. Queda do PSE não derruba busca interna.
6. Reindex faz documento novo aparecer.

## 8. Fora do MVP

App mobile nativo, OCR pesado, IA generativa na busca, SaaS multi-rede global.

## 9. Mapa das tarefas 18.x

| Pasta | Conteúdo |
|-------|----------|
| `docs/18.1-escopo` | Escopo executável e objetivo |
| `docs/18.2-menu-perfis-permissoes` | Menu, módulos, perfis, permissões |
| `docs/18.3-regras-fluxos-integracoes` | Regras, fluxos, integrações |
| `docs/18.4-ux-telas-prototipos` | UX, telas, protótipos |
| `docs/18.5-banco-entidades-rastreabilidade` | Modelo de dados e auditoria |
| `docs/18.6-backend-apis-seguranca` | APIs e segurança |
| `docs/18.7-frontend-paineis` | Frontend e painéis |
| `docs/18.8-instalacao-deploy-recuperacao` | Install, deploy, DR |
| `docs/18.9-manual-uso-operacao-suporte` | Manual operacional |
| `docs/18.10-testes-homologacao-mvp` | Testes e aceite |
| `docs/18.11-repositorios-versionamento-ia` | Git, IA, docs |
| `docs/18.12-referencias-benchmark` | Benchmarks e comparação |

## 10. Stack e versões-alvo

| Camada | Tecnologia | Versão-alvo |
|--------|------------|-------------|
| JDK | Eclipse Temurin | 17+ (21 recomendado) |
| Kotlin | Kotlin | 2.0.x |
| Framework | Spring Boot | 3.3.x |
| DB | PostgreSQL | 16 |
| Frontend | React | 18.x |
| Build UI | Vite | 5.x |
| Linguagem UI | TypeScript | 5.x |
| Migrações | Flyway | (via Spring Boot) |
| Busca web | Google Custom Search JSON API | v1 |

## 11. Self-review

- Sem TBD críticos; decisões A/stack/auth fechadas.
- Arquitetura alinhada às 12 pastas e ao scaffold.
- Escopo MVP delimitado; PSE não é motor de conteúdo privado.
