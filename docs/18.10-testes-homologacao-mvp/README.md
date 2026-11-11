# 18.10 — Testes, homologação, critérios de aceite e MVP

| Campo | Valor |
|-------|-------|
| **Produto** | ByBusca — Buscador Próprio do ecossistema escolar |
| **Etapa** | 8. Buscador Próprio |
| **Tarefa** | 18.10 Testes, homologação, critérios de aceite e MVP |
| **Status** | Pronto para envio |
| **Supervisor** | Luis Rodrigo Lima Rodrigues |
| **Participante** | Pedro Cutrim |
| **Versão** | 1.1 |
| **Depende de** | 18.1–18.9 |
| **Alimenta** | Aceite formal da etapa no cadastro |

---

## 1. Objetivo

Definir a estratégia de testes, os casos prioritários (incluindo ACL e filtro web), o checklist de aceite do MVP e o rito de homologação com evidências para o supervisor.

---

## 2. Estratégia de testes

| Camada | Ferramenta | Foco | Estado no scaffold |
|--------|------------|------|--------------------|
| Unit API | JUnit 5 | ACL, EducationalContentFilter, snippet | ✓ presentes |
| Integração | Testcontainers / Postgres real | FTS + index + search | Manual / smoke |
| API HTTP | Invoke-RestMethod / curl | papéis, status, reindex | Smoke validado |
| Frontend | Build `tsc` + Vite | compilação + rotas | ✓ `npm run build` |
| Manual UI | Checklist §4 | UX, CSE, admin | Homologação |
| Segurança web | Lista exclusão + SafeSearch | adulto/apostas/streaming | ✓ docs + código |

---

## 3. Casos de teste prioritários

### 3.1 Busca e ACL

| ID | Caso | Pré-condição | Esperado |
|----|------|--------------|----------|
| CT01 | GUEST busca doc `public`/`network` | sem token | aparece |
| CT02 | GUEST busca `lista` restrita | sem token | **não** aparece (total 0) |
| CT03 | STUDENT 8A+MAT busca `lista` | login `aluno` | aparece |
| CT04 | Query sem acento `equacoes` | FTS V2+ | encontra “Equações…” |
| CT07 | `q` vazio | — | `400 VALIDATION_ERROR` |
| CT09 | Snippet | busca com termo | `<mark>` no HTML |
| CT11 | Filtro tipo `LESSON` | `types=LESSON` | só aulas |

### 3.2 Auth e admin

| ID | Caso | Esperado |
|----|------|----------|
| CT12 | Login `aluno`/`senha123` | JWT + roles STUDENT |
| CT13 | Login inválido | `401 INVALID_CREDENTIALS` |
| CT10 | Admin health | `documents > 0` |
| CT14 | Reindex ADMIN | `updated >= 1` |
| CT15 | Reindex sem auth admin | `403` |

### 3.3 Indexação

| ID | Caso | Esperado |
|----|------|----------|
| CT08 | Dois POST mesmo `externalId` | 1 linha (upsert) |
| CT16 | Index TEACHER | 200 se permitido |

### 3.4 Web / PSE / filtro educacional

| ID | Caso | Esperado |
|----|------|----------|
| CT05 | Aba Web / widget CSE | resultados com SafeSearch |
| CT06 | PSE JSON offline | internos OK; warning se API enabled |
| CT17 | Query web montada | contém `-apostas` / viés educação |
| CT18 | URL denylist | `pornhub`/`bet365` bloqueados no filtro |
| CT19 | Sites excluídos no CSE | lista `sites-excluidos-cse.txt` aplicada no painel |

### 3.5 Infra

| ID | Caso | Esperado |
|----|------|----------|
| CT20 | `GET /health` | `status: UP` |
| CT21 | Rate limit extremo | `429` após rajada |
| CT22 | Front build | `npm run build` OK |

---

## 4. Critérios de aceite do MVP (checklist)

### Produto / UX
- [ ] Home com marca **ByBusca** + campo único (sem dashboard)
- [ ] Resultados com filtros, snippet e estados vazio/erro/loading
- [ ] Ajuda em `/ajuda`
- [ ] Login + badge + sair

### Busca / ACL
- [ ] Seed demo pesquisável (`Biblioteca`, `equacoes`)
- [ ] CT01–CT04 sem vazamento
- [ ] Login amplia resultados restritos

### Web segura
- [ ] Widget CSE com CX `e54b7047fc7e64e10`
- [ ] SafeSearch ativo
- [ ] Lista de exclusão documentada e (preferencialmente) cadastrada no painel Google
- [ ] Mensagem de busca web educacional visível

### Admin / ops
- [ ] Health índice + reindex
- [ ] Docs 18.1–18.12 presentes
- [ ] Ambiente sobe (Docker **ou** Postgres local)

### Qualidade
- [ ] Testes unitários de filtro/ACL passando
- [ ] API health UP em homologação

---

## 5. Definição de MVP

**MVP ByBusca =**

1. Busca aberta na rede escolar (Postgres FTS + ACL)  
2. Login JWT para conteúdo restrito  
3. Admin mínimo (health + reindex)  
4. Web complementar via Google PSE/CSE com SafeSearch + exclusões  
5. Documentação completa da etapa 8 (18.1–18.12)  
6. Scaffold executável (`apps/api` + `apps/web`)

**Fora do MVP:** mobile nativo, SSO, Elastic, OCR, IA generativa, SaaS multi-rede.

---

## 6. Rito de homologação

| Passo | Responsável | Evidência |
|-------|-------------|-----------|
| 1. Subir homologação na rede | TI | URLs API/Web |
| 2. Executar checklist §4 | Coord + TI | prints / planilha |
| 3. Validar CT ACL (aluno vs visitante) | Coord | print resultados |
| 4. Validar aba Web filtrada | TI | print CSE |
| 5. Aceite formal | Supervisor Luis Rodrigo | assinatura/cadastro |
| 6. Anexar evidências | Participante Pedro | zip/pdf no cadastro |

---

## 7. Evidências sugeridas (anexo)

1. Print home ByBusca  
2. Print busca `equacoes` (interno)  
3. Print visitante sem `lista` + aluno com `lista`  
4. Print `/admin` com reindex  
5. Print aba Web + link engine  
6. Log `GET /health` → UP  
7. Pacote `docs/18.*`  

---

## 8. Critérios de aceite da tarefa 18.10

- [ ] Estratégia e CTs documentados  
- [ ] Checklist MVP completo  
- [ ] Rito de homologação claro  
- [ ] Definição de MVP sem ambiguidade  

## 9. Critério de conclusão

Plano de testes + aceite + homologação prontos para o cadastro → **18.10 concluída / pronta para envio**.
