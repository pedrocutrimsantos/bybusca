# 18.6 — Backend, APIs padronizadas e segurança

| Campo | Valor |
|-------|-------|
| **Produto** | ByBusca — Buscador Próprio do ecossistema escolar |
| **Etapa** | 8. Buscador Próprio |
| **Tarefa** | 18.6 Backend, APIs padronizadas e segurança |
| **Status** | Pronto para envio |
| **Supervisor** | Luis Rodrigo Lima Rodrigues |
| **Participante** | Pedro Cutrim |
| **Versão** | 1.1 |
| **Stack** | Kotlin 2.x · Spring Boot 3.3 · JDK 17+ · PostgreSQL · Flyway · JJWT |
| **Depende de** | 18.3 (RN), 18.5 (dados) |
| **Alimenta** | 18.7 (consumo UI), 18.8 (env/deploy), 18.10 (testes API) |

---

## 1. Objetivo

Documentar a API REST do ByBusca (`apps/api`): pacotes, contratos, autenticação, ACL, rate limit, PSE/filtros educacionais e configuração — alinhado ao código em produção do scaffold.

---

## 2. Pacotes (`br.bybusca`)

```text
br.bybusca
├── ByBuscaApplication.kt          # Boot (+ exclude UserDetails auto-config)
├── config/
│   ├── SecurityConfig.kt          # Security filter chain, CORS, BCrypt
│   ├── ByBuscaProperties.kt       # jwt, pse, defaultTenant
│   ├── AppConfig.kt               # AclService bean
│   └── RateLimitFilter.kt         # 120 req/min por IP em /search|/suggest
├── auth/                          # Login + JWT parse
├── search/                        # SearchController + SearchFacade (FTS)
├── index/                         # Upsert documentos + reindex
├── acl/                           # SearchPrincipal + predicados ACL
├── pse/                           # PseSearchService + EducationalContentFilter
├── domain/                        # Entities + Repositories JPA
└── common/                        # ApiError, HealthController
```

---

## 3. Catálogo de endpoints (`/api/v1`)

| Método | Path | Auth | Papéis | Descrição |
|--------|------|------|--------|-----------|
| GET | `/health` | — | público | Liveness |
| GET | `/search` | opcional | GUEST+ | Busca FTS + ACL (+ PSE API se enabled) |
| GET | `/suggest` | opcional | GUEST+ | Autocomplete títulos |
| POST | `/auth/login` | — | público | Emite JWT |
| POST | `/index/documents` | Bearer | ADMIN/COORD/TEACHER | Upsert documento |
| POST | `/index/reindex` | Bearer | ADMIN/COORD | Rebuild `search_vector` |
| GET | `/admin/health-index` | Bearer | ADMIN/COORD | Contagem docs ativos |

Headers comuns:
- `X-Tenant: demo-escola` (MVP)
- `Authorization: Bearer <jwt>` (quando logado)
- `Content-Type: application/json`

---

## 4. Contratos detalhados

### 4.1 `GET /search`

| Param | Tipo | Default | Regra |
|-------|------|---------|-------|
| `q` | string | — | obrigatório; blank → `400 VALIDATION_ERROR` |
| `types` | CSV | todos | ex.: `LESSON,FILE` |
| `page` | int | 0 | ≥0 |
| `size` | int | 20 | 1–50 |
| `includeWeb` | bool | false | chama PSE JSON se `GOOGLE_PSE_ENABLED` |

**200**

```json
{
  "items": [
    {
      "id": "uuid-or-url",
      "type": "LESSON",
      "title": "Equações do 1º grau",
      "snippet": "... <mark>equações</mark> ...",
      "url": "https://escola.local/aulas/mat-01",
      "score": 0.92,
      "source": "internal"
    }
  ],
  "page": 0,
  "size": 20,
  "total": 1,
  "tookMs": 48,
  "warnings": []
}
```

`source`: `internal` | `pse`  
`warnings` possíveis: `PSE_UNAVAILABLE`, `TENANT_NOT_FOUND`

### 4.2 `GET /suggest?q=`

Retorna até 8 itens `{ "text", "type" }` de títulos `public|network`.

### 4.3 `POST /auth/login`

```json
{ "username": "aluno", "password": "senha123" }
```

```json
{
  "accessToken": "<jwt>",
  "expiresIn": 28800,
  "user": {
    "id": "...",
    "username": "aluno",
    "displayName": "Aluno João",
    "roles": ["STUDENT"]
  }
}
```

Claims JWT: `sub`, `tenant`, `roles`, `classIds`, `subjectIds`, `name`, `exp`.

### 4.4 `POST /index/documents`

Idempotente por `(tenant_id, external_id)`. Atualiza ACL + `portuguese_unaccent`.

### 4.5 `POST /index/reindex`

```json
{ "tenant": "demo-escola", "updated": 6, "tookMs": 12 }
```

### 4.6 Erros padronizados

```json
{ "code": "FORBIDDEN", "message": "…", "traceId": "…" }
```

| code | HTTP |
|------|------|
| `VALIDATION_ERROR` | 400 |
| `INVALID_CREDENTIALS` | 401 |
| `FORBIDDEN` | 403 |
| `TENANT_NOT_FOUND` | 404 |
| `RATE_LIMITED` | 429 |
| `INTERNAL_ERROR` | 500 |

---

## 5. Segurança

| Controle | Implementação |
|----------|---------------|
| AuthN | JWT HMAC (`JWT_SECRET`) |
| AuthZ busca | ACL SQL (omitir sem permissão) |
| AuthZ admin/index | roles no JWT |
| Senhas | BCrypt |
| CORS | `localhost` / `127.0.0.1` patterns |
| Rate limit | `RateLimitFilter` 120/min em search/suggest |
| Secrets | env only (nunca no front) |
| PSE | query sanitizada + `safe=active` + denylist (`EducationalContentFilter`) |
| Validação | Bean Validation |
| CSRF | desabilitado (API stateless) |

### Anti-vazamento
1. ACL antes do merge/ranking  
2. Sem stack/SQL no client  
3. PSE recebe só a query textual (com filtros educacionais), nunca bodies internos  

---

## 6. Busca interna (FTS)

- Config: `portuguese_unaccent`  
- Query: `websearch_to_tsquery`  
- Ranking: `ts_rank_cd`  
- Soft-delete: `deleted_at IS NULL`  

---

## 7. Variáveis de ambiente

| Variável | Obrigatória | Uso |
|----------|:-----------:|-----|
| `DB_URL` | sim | JDBC Postgres |
| `DB_USER` / `DB_PASSWORD` | sim | Credenciais DB |
| `JWT_SECRET` | sim (≥32 bytes) | Assinatura |
| `BYBUSCA_DEFAULT_TENANT` | não | default `demo-escola` |
| `GOOGLE_PSE_CX` | sim (web) | `e54b7047fc7e64e10` |
| `GOOGLE_PSE_API_KEY` | não | JSON API |
| `GOOGLE_PSE_ENABLED` | não | default `false` |
| `GOOGLE_PSE_TIMEOUT_MS` | não | default `2500` |
| `SERVER_PORT` | não | default `8080` |

---

## 8. Como rodar

```powershell
cd apps\api
$env:DB_URL='jdbc:postgresql://localhost:5432/bybusca'
$env:DB_USER='postgres'
$env:DB_PASSWORD='…'
.\gradlew.bat bootRun
```

Health: `GET http://localhost:8080/api/v1/health`

---

## 9. Critérios de aceite da 18.6

- [ ] Catálogo de endpoints completo e coerente com o código  
- [ ] Contratos JSON + códigos de erro documentados  
- [ ] Controles de segurança e ACL descritos  
- [ ] Env vars e PSE/filtro educacional referenciados  
- [ ] Rate limit e reindex documentados  

## 10. Critério de conclusão

API, segurança e configuração alinhadas ao scaffold → **18.6 concluída / pronta para envio**.
