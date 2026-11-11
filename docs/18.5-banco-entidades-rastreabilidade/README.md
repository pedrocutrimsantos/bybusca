# 18.5 — Banco de dados, entidades e rastreabilidade

| Campo | Valor |
|-------|-------|
| **Produto** | ByBusca — Buscador Próprio do ecossistema escolar |
| **Etapa** | 8. Buscador Próprio |
| **Tarefa** | 18.5 Banco de dados, entidades e rastreabilidade |
| **Status** | Pronto para envio |
| **Supervisor** | Luis Rodrigo Lima Rodrigues |
| **Participante** | Pedro Cutrim |
| **Versão** | 1.1 |
| **SGBD** | PostgreSQL 16+ (validado em 18.3 local) |
| **Migrações** | Flyway (`V1`–`V3`) |
| **Depende de** | 18.2 (ACL), 18.3 (RN) |
| **Alimenta** | 18.6 (APIs), 18.10 (testes), 18.8 (backup) |

---

## 1. Objetivo da tarefa

Especificar o **modelo relacional**, **FTS**, **índices**, **seed**, **rastreabilidade/auditoria** e **migrações** do ByBusca, alinhados à implementação em `apps/api/src/main/resources/db/migration/`.

---

## 2. Diagrama lógico

```text
tenants 1──* users
tenants 1──* documents
tenants 1──* search_audit

users *──* roles                 (user_roles)
users 1──* (atributos)           class_ids[], subject_ids[]

documents 1──1 document_acl
documents.search_vector          TSVECTOR (GIN)

extensões: pgcrypto, unaccent
config FTS: portuguese_unaccent
```

---

## 3. Migrações Flyway

| Versão | Arquivo | Conteúdo |
|--------|---------|----------|
| V1 | `V1__init.sql` | Schema + roles + tenant demo + users + docs + ACL + GIN |
| V2 | `V2__fts_unaccent.sql` | `unaccent` + config `portuguese_unaccent` + rebuild vectors |
| V3 | `V3__fix_demo_acl_attrs.sql` | Ajusta `aluno`/`prof` (turma/disciplina) para demos ACL |

**Regra:** não editar migração já aplicada em ambientes existentes (checksum). Novas mudanças = nova versão `Vn`.

---

## 4. Entidades (dicionário de dados)

### 4.1 `tenants`

| Coluna | Tipo | Restrições | Descrição |
|--------|------|------------|-----------|
| id | UUID | PK, default `gen_random_uuid()` | Identificador |
| code | VARCHAR(64) | UK NOT NULL | Código curto (`demo-escola`) |
| name | VARCHAR(200) | NOT NULL | Nome de exibição |
| created_at | TIMESTAMPTZ | NOT NULL default now() | Criação |

### 4.2 `users`

| Coluna | Tipo | Restrições | Descrição |
|--------|------|------------|-----------|
| id | UUID | PK | |
| tenant_id | UUID | FK → tenants | Escola |
| username | VARCHAR(100) | UK (tenant_id, username) | Login |
| password_hash | VARCHAR(255) | NOT NULL | BCrypt |
| display_name | VARCHAR(200) | NOT NULL | Nome UI |
| active | BOOLEAN | default true | |
| class_ids | TEXT[] | default `{}` | Turmas do usuário |
| subject_ids | TEXT[] | default `{}` | Disciplinas |

### 4.3 `roles` / `user_roles`

| Tabela | Colunas | Notas |
|--------|---------|-------|
| roles | id, code UK, name | `STUDENT`, `TEACHER`, `COORDINATOR`, `ADMIN` |
| user_roles | user_id, role_id | PK composta; CASCADE delete |

> `GUEST` não é linha de banco — é papel derivado sem JWT.

### 4.4 `documents`

| Coluna | Tipo | Restrições | Descrição |
|--------|------|------------|-----------|
| id | UUID | PK | |
| tenant_id | UUID | FK | |
| external_id | VARCHAR(120) | UK (tenant_id, external_id) | Idempotência origem |
| type | VARCHAR(32) | NOT NULL | `LESSON`, `COLLECTION`, `FILE`, `PAGE`, `MODULE`, `CONTENT` |
| title | VARCHAR(500) | NOT NULL | Peso A no FTS |
| body | TEXT | default '' | Peso B no FTS |
| url | VARCHAR(1000) | | Destino do clique |
| visibility | VARCHAR(20) | default `network` | `public`/`network`/`restricted` |
| metadata | JSONB | default `{}` | ex.: subject, grade |
| search_vector | TSVECTOR | | Índice invertido |
| indexed_at | TIMESTAMPTZ | | Última indexação |
| deleted_at | TIMESTAMPTZ | NULL = ativo | Soft-delete |

**Índices**

- `GIN (search_vector)`  
- `(tenant_id, type) WHERE deleted_at IS NULL`

### 4.5 `document_acl`

| Coluna | Tipo | Semântica |
|--------|------|-----------|
| document_id | UUID PK/FK | 1:1 com document |
| allowed_roles | TEXT[] | vazio = sem filtro de role |
| allowed_class_ids | TEXT[] | vazio = sem filtro de turma |
| allowed_subject_ids | TEXT[] | vazio = sem filtro de disciplina |
| owner_user_id | UUID NULL | opcional |

### 4.6 `search_audit`

| Coluna | Tipo | Descrição |
|--------|------|-----------|
| id | BIGSERIAL | |
| tenant_id | UUID FK | |
| user_id | UUID NULL | null = visitante |
| query | VARCHAR(500) | termo buscado |
| ip | VARCHAR(64) | origem |
| took_ms | INT | latência |
| sources | TEXT[] | `internal`, `pse` |
| created_at | TIMESTAMPTZ | |

---

## 5. Full-Text Search

### 5.1 Configuração

```sql
CREATE EXTENSION IF NOT EXISTS unaccent;
CREATE TEXT SEARCH CONFIGURATION portuguese_unaccent (COPY = portuguese);
ALTER TEXT SEARCH CONFIGURATION portuguese_unaccent
  ALTER MAPPING FOR hword, hword_part, word
  WITH unaccent, portuguese_stem;
```

### 5.2 Geração do vetor

```sql
search_vector =
  setweight(to_tsvector('portuguese_unaccent', coalesce(title,'')), 'A') ||
  setweight(to_tsvector('portuguese_unaccent', coalesce(body,'')), 'B');
```

### 5.3 Consulta (API)

```sql
search_vector @@ websearch_to_tsquery('portuguese_unaccent', :q)
ORDER BY ts_rank_cd(search_vector, websearch_to_tsquery('portuguese_unaccent', :q)) DESC
```

**Efeito:** `equacoes` encontra “Equações…” (RN14).

### 5.4 Rebuild

`POST /api/v1/index/reindex` recalcula `search_vector` de todos os docs ativos do tenant.

---

## 6. Rastreabilidade e LGPD

| Evento | Onde fica | Campos-chave |
|--------|-----------|--------------|
| Busca | `search_audit` | q, user?, ip, took_ms, sources |
| Indexação | `documents.indexed_at` + `external_id` | origem estável |
| Soft-delete | `deleted_at` | some da busca |
| Auth | JWT efêmero | sem senha em log |

**Retenção sugerida (MVP doc):** 90 dias para `search_audit`  
**Purge:** job futuro (`DELETE WHERE created_at < now() - interval '90 days'`)  
**Seed:** dados sintéticos — sem PII real de alunos.

---

## 7. Seed MVP (`demo-escola`)

### 7.1 Usuários (senha dev: `senha123`)

| username | role | class_ids | subject_ids |
|----------|------|-----------|-------------|
| admin | ADMIN | {} | {} |
| prof | TEACHER | {8A} *após V3* | {MAT} |
| aluno | STUDENT | {8A} | {MAT} *após V3* |

### 7.2 Documentos (amostra)

| external_id | type | visibility | Quem vê |
|-------------|------|------------|---------|
| aula-mat-01 | LESSON | network | todos na rede |
| acervo-01 | COLLECTION | public | todos |
| arquivo-plano | FILE | restricted | TEACHER/ADMIN/COORD + MAT |
| pagina-biblioteca | PAGE | network | todos |
| modulo-hist | MODULE | network | todos |
| aula-8a-privada | LESSON | restricted | STUDENT/TEACHER/ADMIN + 8A + MAT |

---

## 8. Integridade e performance

| Tema | Decisão |
|------|---------|
| Idempotência | UK `(tenant_id, external_id)` |
| Cascata ACL | `ON DELETE CASCADE` em `document_acl` |
| Soft-delete | busca filtra `deleted_at IS NULL` |
| FTS | GIN obrigatório |
| Arrays ACL | operadores `&&` (overlap) |
| Multi-tenant | sempre filtrar por `tenant_id` |

---

## 9. Backup / restore (ligação 18.8)

| Ativo | Método |
|-------|--------|
| Schema+dados | `pg_dump` / `pg_restore` |
| Migrações | versionadas no Git (Flyway) |
| Secrets | fora do dump de app (env) |

---

## 10. Critérios de aceite da 18.5

- [ ] Diagrama e dicionário cobrem todas as tabelas do scaffold  
- [ ] FTS `portuguese_unaccent` documentado e aplicado (V2)  
- [ ] Seed + V3 coerentes com demos ACL  
- [ ] Auditoria e retenção LGPD descritas  
- [ ] Política de migrações (não editar Vn aplicada) explícita  

## 11. Critério de conclusão

Modelo, FTS, rastreabilidade e seeds alinhados ao código Flyway → **18.5 concluída / pronta para envio**.
