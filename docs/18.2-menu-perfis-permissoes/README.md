# 18.2 — Menu padrão, módulos, perfis e permissões

| Campo | Valor |
|-------|-------|
| **Produto** | ByBusca — Buscador Próprio do ecossistema escolar |
| **Etapa** | 8. Buscador Próprio |
| **Tarefa** | 18.2 Menu padrão, módulos, perfis e permissões |
| **Status** | Pronto para envio |
| **Supervisor** | Luis Rodrigo Lima Rodrigues |
| **Participante** | Pedro Cutrim |
| **Versão** | 1.1 |
| **Depende de** | 18.1 Escopo |
| **Alimenta** | 18.3 (regras), 18.6 (segurança), 18.7 (menus UI) |

---

## 1. Objetivo da tarefa

Definir a **navegação padrão**, os **módulos funcionais**, os **perfis (roles)** e a **matriz de permissões/ACL** do ByBusca, de forma alinhada ao escopo 18.1 e implementável no scaffold (`apps/api` + `apps/web`).

**Resultado:** modelo RBAC + ACL documental suficiente para busca aberta na rede e conteúdo restrito por turma/disciplina/papel.

---

## 2. Princípios de autorização

| ID | Princípio |
|----|-----------|
| AZ-01 | Busca aberta na rede: visitante acessa `public` e `network` sem login |
| AZ-02 | Conteúdo `restricted` só com JWT válido + match de ACL |
| AZ-03 | Sem permissão → **omitir** o documento (não 403 item a item) |
| AZ-04 | Isolamento por `tenant_id` (escola/rede da instalação) |
| AZ-05 | Secrets PSE nunca no frontend; só ADMIN opera config via ambiente |
| AZ-06 | Papéis são cumulativos via `user_roles` (um usuário pode ter 1+ roles) |

---

## 3. Módulos do sistema

| Código | Módulo | Responsabilidade | Superfície |
|--------|--------|------------------|------------|
| `SEARCH` | Busca | Query, filtros, snippets, ranking, merge PSE | `/`, API `/search` |
| `SUGGEST` | Sugestões | Autocomplete de títulos permitidos | campo busca, `/suggest` |
| `INDEX` | Indexação | Ingestão/upsert/reindex de documentos | `/admin`, `/index/*` |
| `ACL` | Permissões | Avaliação tenant/papel/turma/disciplina | serviços backend |
| `AUTH` | Autenticação | Login JWT, sessão no browser | `/login` |
| `ADMIN` | Administração | Health do índice, usuários (visão), auditoria | `/admin` |
| `WEB` | Busca web | Fonte Google PSE (pública) | aba Web / `includeWeb` |
| `AUDIT` | Auditoria | Registro de consultas | tabela `search_audit` |

### 3.1 Dependências entre módulos

```text
AUTH ──► ACL ──► SEARCH ──► AUDIT
                 SEARCH ──► WEB (PSE opcional)
INDEX ──► ACL (quem pode indexar)
ADMIN ──► INDEX + AUDIT + AUTH
```

---

## 4. Menu padrão (IA / navegação)

### 4.1 Estrutura global

| Item de menu | Rota | GUEST | STUDENT | TEACHER | COORD | ADMIN |
|--------------|------|:-----:|:-------:|:-------:|:-----:|:-----:|
| ByBusca (home/busca) | `/` | ✓ | ✓ | ✓ | ✓ | ✓ |
| Entrar | `/login` | ✓ | —* | —* | —* | —* |
| Sair | ação | — | ✓ | ✓ | ✓ | ✓ |
| Painel admin | `/admin` | — | — | — | ✓ | ✓ |
| Ajuda (link docs) | externo/MVP texto | ✓ | ✓ | ✓ | ✓ | ✓ |

\*Logado: “Entrar” some; aparece badge do nome + “Sair”.

### 4.2 Menu por persona (MVP implementado)

**Visitante / Aluno (rede)**  
1. Início (busca)  
2. Entrar (se visitante)  
3. Ajuda rápida (manual 18.9)

**Professor**  
1. Início (busca ampliada)  
2. Sair  
3. Ajuda  

**Coordenação / Admin**  
1. Início  
2. Painel admin (status índice, reindex, orientação PSE)  
3. Sair  
4. Ajuda / suporte  

### 4.3 Itens de menu previstos (pós-MVP / roadmap)

| Item | Motivo de ficar fora do MVP |
|------|-----------------------------|
| Meus filtros salvos (cloud) | MVP usa no máximo localStorage |
| Gestão completa de usuários (CRUD) | Admin mínimo: health + reindex |
| Tela visual de config PSE | Secrets só em env (18.8) |
| Dashboard de auditoria gráfica | Auditoria em tabela; UI lista depois |

---

## 5. Perfis (roles)

| Role | Código JWT | Quem é | Escopo típico de leitura |
|------|------------|--------|--------------------------|
| Visitante | `GUEST` | Sem token / token inválido | `public`, `network` |
| Aluno | `STUDENT` | Estudante da escola | + `restricted` da(s) turma(s) e disciplina(s) |
| Professor | `TEACHER` | Docente | + materiais da disciplina (+ turma se vinculada) |
| Coordenação | `COORDINATOR` | Coord. pedagógica/TI pedagógica | Visão ampla da escola (sem secrets) |
| Administrador | `ADMIN` | TI / admin do tenant | Gestão total do tenant |

### 5.1 Atributos de contexto (além do role)

Armazenados no usuário e embutidos no JWT:

| Atributo | Uso na ACL |
|----------|------------|
| `tenant` / `tenant_id` | Isola a escola |
| `classIds` | Match com `document_acl.allowed_class_ids` |
| `subjectIds` | Match com `document_acl.allowed_subject_ids` |
| `roles[]` | Match com `document_acl.allowed_roles` |

**Seed demo**

| Usuário | Role | classIds | subjectIds |
|---------|------|----------|------------|
| `aluno` | STUDENT | `8A` | `MAT` |
| `prof` | TEACHER | `8A` | `MAT` |
| `admin` | ADMIN | — | — |

Senha demo: `senha123` (somente desenvolvimento).

---

## 6. Matriz de permissões (RBAC de API/UI)

| Permissão | Código | GUEST | STUDENT | TEACHER | COORD | ADMIN |
|-----------|--------|:-----:|:-------:|:-------:|:-----:|:-----:|
| Buscar público | `search:public` | ✓ | ✓ | ✓ | ✓ | ✓ |
| Buscar rede | `search:network` | ✓ | ✓ | ✓ | ✓ | ✓ |
| Buscar restrito | `search:restricted` | — | ✓* | ✓* | ✓ | ✓ |
| Buscar web PSE | `search:web_pse` | ✓ | ✓ | ✓ | ✓ | ✓ |
| Sugestões | `search:suggest` | ✓ | ✓ | ✓ | ✓ | ✓ |
| Indexar documento | `index:write` | — | — | limitado** | ✓ | ✓ |
| Reindex tenant | `index:reindex` | — | — | — | ✓ | ✓ |
| Health do índice | `admin:health` | — | — | — | ✓ | ✓ |
| Ver usuários | `admin:users` | — | — | — | leitura | ✓ |
| Config PSE (env) | `admin:pse_config` | — | — | — | — | ✓ |
| Ver auditoria | `admin:audit` | — | — | — | ✓ | ✓ |

\*Restrito só se ACL do documento casar com turma/disciplina/papel.  
\*\*Professor: apenas documentos da própria disciplina (MVP: habilitado na API; política default pode restringir a ADMIN/COORD).

---

## 7. Modelo ACL de documento

### 7.1 Campos

| Campo | Valores / tipo | Semântica |
|-------|----------------|-----------|
| `tenant_id` | UUID | Obrigatório |
| `visibility` | `public` \| `network` \| `restricted` | Porta de entrada da ACL |
| `allowed_roles` | `text[]` | Vazio = não restringe por role |
| `allowed_class_ids` | `text[]` | Vazio = não restringe por turma |
| `allowed_subject_ids` | `text[]` | Vazio = não restringe por disciplina |
| `owner_user_id` | UUID? | Opcional (rastreio) |

### 7.2 Algoritmo (resumo)

1. Se `visibility ∈ {public, network}` → visível a GUEST e autenticados do tenant  
2. Se `visibility = restricted` → exige usuário autenticado **e**  
   - (`allowed_roles` vazio **OU** overlap com roles do usuário) **E**  
   - (`allowed_class_ids` vazio **OU** overlap com classIds) **E**  
   - (`allowed_subject_ids` vazio **OU** overlap com subjectIds)  
3. Caso contrário → documento omitido do resultado  

### 7.3 Exemplos

| Documento | visibility | ACL | Quem vê |
|-----------|------------|-----|---------|
| Acervo ciências | `public` | — | Todos na rede |
| Aula equações | `network` | — | Todos na rede |
| Plano PDF | `restricted` | roles TEACHER/ADMIN/COORD; subject MAT | Prof/Admin/Coord com MAT |
| Lista 8A | `restricted` | roles STUDENT/TEACHER/ADMIN; class 8A; subject MAT | Aluno 8A+MAT, Prof 8A+MAT, Admin |

**Regra de ouro:** se não puder ver, **não aparece**.

---

## 8. Multi-escola / rede

| Aspecto | MVP | Evolução |
|---------|-----|----------|
| Deploy | 1 instalação = 1 escola/rede | Multi-tenant SaaS |
| Isolamento | `tenant_id` + header `X-Tenant` | Subdomínio por escola |
| Busca cross-escola | Não | Somente com ACL explícita de rede |

Campo `tenant_id` já existe para não redesenhar depois.

---

## 9. Mapeamento para o scaffold

| Conceito 18.2 | Implementação |
|---------------|---------------|
| Roles | tabela `roles` + `user_roles` + claims JWT |
| Menus | rotas React `/`, `/login`, `/admin` |
| ACL | `AclService` + SQL em `SearchFacade` |
| Admin | `GET /admin/health-index`, `POST /index/reindex` |
| Visitante | ausência/invalidade de token → `GUEST` |

---

## 10. Critérios de aceite da 18.2

- [ ] Módulos e menus por persona documentados  
- [ ] Matriz RBAC completa e sem ambiguidade  
- [ ] Algoritmo ACL de documento descrito com exemplos  
- [ ] Seed demo coerente (aluno 8A+MAT, prof 8A+MAT, admin)  
- [ ] Regra “omitir sem permissão” explícita  
- [ ] Documento alinhado a 18.1 e às APIs reais  

## 11. Critério de conclusão

Com seções 1–10 aprováveis pelo supervisor e refletidas no código de auth/ACL, a tarefa **18.2** está **concluída e pronta para envio**.
