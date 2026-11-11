# 18.3 — Regras de negócio, fluxos e integrações

| Campo | Valor |
|-------|-------|
| **Produto** | ByBusca — Buscador Próprio do ecossistema escolar |
| **Etapa** | 8. Buscador Próprio |
| **Tarefa** | 18.3 Regras de negócio, fluxos e integrações |
| **Status** | Pronto para envio |
| **Supervisor** | Luis Rodrigo Lima Rodrigues |
| **Participante** | Pedro Cutrim |
| **Versão** | 1.1 |
| **Depende de** | 18.1 Escopo, 18.2 Perfis/ACL |
| **Alimenta** | 18.5 (dados), 18.6 (APIs), 18.7 (UI), 18.10 (testes) |

---

## 1. Objetivo da tarefa

Formalizar as **regras de negócio (RN)**, os **fluxos ponta a ponta** e os **contratos de integração** (PSE, ingestão escolar, rede) do ByBusca, de modo que backend, frontend e homologação compartilhem a mesma verdade operacional.

---

## 2. Regras de negócio

### 2.1 Catálogo RN

| ID | Regra | Severidade |
|----|-------|------------|
| RN01 | Toda operação de busca/index exige `tenant` resolvido (`X-Tenant`, claim JWT ou default `BYBUSCA_DEFAULT_TENANT`). | Alta |
| RN02 | Sem token ou token inválido → sujeito `GUEST` (não quebra a busca). | Alta |
| RN03 | ACL é aplicada **antes** do ranking/merge final. | Alta |
| RN04 | PSE só é consultado se `includeWeb=true` ou filtro/aba Web. | Média |
| RN05 | Conteúdo interno/privado **nunca** é enviado ao Google/PSE. | Crítica |
| RN06 | Indexação é idempotente por (`tenant_id`, `external_id`). | Alta |
| RN07 | `q` vazio/blank → `400 VALIDATION_ERROR`. | Média |
| RN08 | Paginação: default `size=20`, máximo `50`; `page>=0`. | Baixa |
| RN09 | Snippet ≤ 240 caracteres, com destaque (`<mark>`) dos termos. | Média |
| RN10 | Falha/timeout/quota PSE → resposta 200 com internos + `warnings: ["PSE_UNAVAILABLE"]`. | Alta |
| RN11 | COORD/ADMIN podem disparar reindex do tenant (reconstrução de `search_vector`). | Média |
| RN12 | Toda busca gera auditoria: query, user?, IP, `took_ms`, sources. | Média |
| RN13 | Soft-delete (`deleted_at`) remove o documento das buscas sem apagar histórico imediato. | Média |
| RN14 | FTS usa configuração `portuguese_unaccent` (busca sem depender de acento). | Alta |
| RN15 | Rate limit básico por IP protege `/search` e `/suggest` na rede. | Média |
| RN16 | Login bem-sucedido emite JWT com `roles`, `classIds`, `subjectIds`, `tenant`. | Alta |
| RN17 | Credenciais inválidas → `401 INVALID_CREDENTIALS` (mensagem genérica). | Alta |
| RN19 | Busca web aplica SafeSearch + exclusão de adulto/apostas/streaming e viés educacional/científico. | Alta |

### 2.2 Prioridade de fontes no merge

1. Resultados **internos** (Postgres FTS) ordenados por `ts_rank_cd`  
2. Resultados **PSE** (score base menor; complementares)  
3. Lista final ordenada por `score` desc; warnings anexados  

---

## 3. Fluxos detalhados

### 3.1 Fluxo — Busca (visitante e logado)

```text
[UI] GET /api/v1/search?q=&types=&page=&size=&includeWeb=
        │
        ├─ Resolve tenant (X-Tenant | default)
        ├─ Resolve principal (JWT | GUEST)          ← RN02
        ├─ Valida q                                ← RN07
        ├─ Monta filtro ACL                        ← RN03, 18.2
        ├─ FTS portuguese_unaccent                 ← RN14
        ├─ [includeWeb] PseSearchService(timeout)  ← RN04, RN05, RN10
        ├─ Merge + rank
        ├─ Audit                                   ← RN12
        └─ 200 { items, page, size, total, tookMs, warnings }
```

**Pós-condições**

- Visitante nunca recebe `restricted`  
- Aluno/prof só recebe `restricted` permitido  
- PSE ausente não gera 5xx  

### 3.2 Fluxo — Sugestão

```text
[UI digita ≥2 chars] → GET /suggest?q=
  → tenant + principal
  → títulos public/network (MVP suggest aberto)
  → até 8 itens { text, type }
```

### 3.3 Fluxo — Login

```text
POST /api/v1/auth/login { username, password }
  → localiza user no tenant
  → BCrypt matches
  → JWT (sub, tenant, roles, classIds, subjectIds, name, exp)
  → 200 { accessToken, expiresIn, user }
```

UI: guarda token em `sessionStorage` (não localStorage permanente).

### 3.4 Fluxo — Indexação (upsert)

```text
POST /api/v1/index/documents  (+ Bearer ADMIN|COORD|TEACHER)
  → valida payload
  → INSERT ... ON CONFLICT (tenant_id, external_id) DO UPDATE
  → upsert document_acl
  → search_vector = to_tsvector('portuguese_unaccent', title/body)
  → 200 { id, externalId, indexedAt }
```

### 3.5 Fluxo — Reindex do tenant

```text
POST /api/v1/index/reindex  (+ Bearer ADMIN|COORD)
  → UPDATE documents SET search_vector = ... WHERE tenant_id=? AND deleted_at IS NULL
  → 200 { tenant, updated, tookMs }
```

Uso: após mudança de config FTS, carga em lote ou correção operacional (RN11).

### 3.6 Fluxo — Degradação PSE

```text
includeWeb=true
  → PseClient (connect/read timeout ~2.5s)
  → sucesso: items source=pse
  → falha: failures++ ; se >=3 circuit open
  → warnings += PSE_UNAVAILABLE
  → internos retornam normalmente
```

---

## 4. Contratos de API (resumo executável)

### 4.1 `GET /api/v1/search`

| Param | Obrigatório | Default | Notas |
|-------|:-----------:|---------|-------|
| `q` | sim | — | trim; RN07 |
| `types` | não | todos | CSV: `LESSON,FILE,...` |
| `page` | não | 0 | |
| `size` | não | 20 | max 50 |
| `includeWeb` | não | false | |

Headers: `X-Tenant`, `Authorization?`

### 4.2 `GET /api/v1/suggest`

| Param | Obrigatório |
|-------|:-----------:|
| `q` | sim (≥2 úteis) |

### 4.3 `POST /api/v1/auth/login`

Body: `{ "username", "password" }`

### 4.4 `POST /api/v1/index/documents`

Body mínimo:

```json
{
  "externalId": "aula-123",
  "type": "LESSON",
  "title": "Equações",
  "body": "...",
  "url": "https://escola.local/...",
  "visibility": "network",
  "allowedRoles": [],
  "allowedClassIds": [],
  "allowedSubjectIds": [],
  "metadata": {}
}
```

### 4.5 `POST /api/v1/index/reindex`

Sem body. Auth COORD/ADMIN.

### 4.6 `GET /api/v1/admin/health-index`

Retorna `{ tenant, documents, status }`.

### 4.7 `GET /api/v1/health`

Liveness sem auth.

---

## 5. Integrações

### 5.1 Google Programmable Search (PSE)

| Aspecto | Definição |
|---------|-----------|
| Engine ID (`cx`) | `e54b7047fc7e64e10` |
| URL pública | https://cse.google.com/cse?cx=e54b7047fc7e64e10 |
| Widget (front) | `cse.js?cx=…` com `safeSearch=active` + query filtrada |
| Filtro conteúdo | Denylist (adulto/apostas/streaming) + operadores negativos + viés “educação/ciência” |
| API (back, opcional) | Custom Search JSON API v1 com `safe=active` + pós-filtro de URL |
| Endpoint API | `https://www.googleapis.com/customsearch/v1` |
| Params API | `key`, `cx`, `q`, `num` |
| Env | `GOOGLE_PSE_CX`, `VITE_GOOGLE_PSE_CX`, `GOOGLE_PSE_API_KEY?`, `GOOGLE_PSE_ENABLED` |
| Timeout API | `GOOGLE_PSE_TIMEOUT_MS` (default 2500) |
| Proibido | enviar body/ACL/metadados internos sensíveis ao Google |

### 5.2 Ecossistema escolar (ingestão)

| Aspecto | Definição |
|---------|-----------|
| Contrato | `POST /api/v1/index/documents` |
| Auth MVP | JWT ADMIN/COORD/TEACHER |
| Auth futuro | API key de serviço |
| Tipos | `CONTENT`, `COLLECTION`, `LESSON`, `FILE`, `PAGE`, `MODULE` |
| Idempotência | `externalId` estável na origem |

### 5.3 Rede da escola (mecanismo de busca)

| Aspecto | Definição |
|---------|-----------|
| Publicação | URL interna do frontend (ex. `http://busca.escola.local`) |
| Proxy | Nginx/`/api/` → Spring Boot |
| Browser | pode registrar ByBusca como search engine da rede |
| Premissa | catálogo `public`/`network` acessível na LAN |

---

## 6. Estados, exceções e inconsistências

| Situação | Comportamento esperado | RN |
|----------|------------------------|----|
| Documento soft-deleted | Fora do FTS/busca | RN13 |
| ACL alterada | Próxima busca já reflete (sem cache de resultado no MVP) | RN03 |
| PSE timeout/quota | Warning + só internos | RN10 |
| Token expirado | Trata como GUEST | RN02 |
| Tenant inexistente | Warning/erro controlado `TENANT_NOT_FOUND` | RN01 |
| Index duplicado mesmo `externalId` | Upsert (sem duplicar linha) | RN06 |
| Rate limit excedido | `429 RATE_LIMITED` | RN15 |

---

## 7. Matriz RN → evidência de teste (ligação com 18.10)

| RN | Caso de teste sugerido |
|----|------------------------|
| RN02/RN03 | GUEST não vê `lista` restrita; STUDENT 8A+MAT vê |
| RN05/RN10 | PSE off/ falha → busca interna OK |
| RN06 | Dois POSTs mesmo `externalId` → 1 linha |
| RN07 | `q=` → 400 |
| RN11 | Reindex ADMIN → `updated > 0` |
| RN14 | `q=equacoes` encontra “Equações…” |
| RN16 | Login aluno retorna token com roles/class/subject |

---

## 8. Critérios de aceite da 18.3

- [ ] Catálogo RN01–RN18 completo e sem contradição com 18.1/18.2  
- [ ] Fluxos de busca, login, index, reindex e degradação PSE descritos  
- [ ] Contratos de API alinhados ao código em `apps/api`  
- [ ] Integrações PSE / ingestão / rede especificadas  
- [ ] Tratamento de inconsistências definido  

## 9. Critério de conclusão

Com as seções 1–8 revisadas e aderentes à implementação atual, a tarefa **18.3** está **concluída e pronta para envio**.
