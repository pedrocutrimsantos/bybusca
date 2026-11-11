# 18.1 — Escopo executável e objetivo da etapa

| Campo | Valor |
|-------|-------|
| **Produto** | ByBusca — Buscador Próprio do ecossistema escolar |
| **Etapa** | 8. Buscador Próprio |
| **Tarefa** | 18.1 Escopo executável e objetivo da etapa |
| **Status** | Pronto para envio |
| **Supervisor (aprovação)** | Luis Rodrigo Lima Rodrigues |
| **Participante** | Pedro Cutrim |
| **Data de referência** | 2026-07-24 |
| **Versão do documento** | 1.1 |

---

## 1. Objetivo da etapa

### 1.1 Objetivo geral

Criar e documentar o **Buscador Próprio (ByBusca)** do ecossistema escolar: um mecanismo de busca com experiência **tipo Google**, voltado à **rede da escola**, capaz de:

1. **Indexar** conteúdos, acervo, aulas, arquivos, páginas e módulos do ambiente escolar  
2. **Buscar com relevância** (ranking e snippets)  
3. **Respeitar ACL** (quem pode ver o quê: aluno, professor, coordenação, admin, visitante)  
4. **Complementar** com busca **web pública** via **Google Programmable Search (PSE)**, sem expor acervo privado ao Google  

O ByBusca deve funcionar como **mecanismo de busca padrão da rede escolar**: aberto na LAN/intranet da escola, com camada opcional de login para materiais restritos.

### 1.2 Objetivo específico desta tarefa (18.1)

Formalizar, de forma **executável e auditável**, o escopo da etapa 8:

- O que será feito (incluso)  
- O que não será feito (excluído)  
- Para quem (personas)  
- Com quais métricas de sucesso  
- Sob quais premissas, restrições, riscos e entregáveis  
- Como esta tarefa se conecta às demais (18.2–18.12)  

**Resultado esperado da 18.1:** documento de escopo aprovável pelo supervisor, alinhado ao design técnico e suficiente para orientar implementação, homologação e envio no cadastro da etapa.

### 1.3 Declaração de valor (por quê existe)

Escolas concentram conhecimento em vários sistemas (LMS, drive, páginas, módulos, arquivos). Sem um ponto único de busca:

- Aluno e professor perdem tempo procurando material  
- Coordenação não tem visão unificada do acervo  
- Material sensível não pode ir para buscadores públicos  
- A rede escolar não tem um “Google da escola” confiável  

O ByBusca resolve isso com **busca unificada + permissões + web pública controlada**.

---

## 2. Contexto e posicionamento

### 2.1 Contexto da etapa 8

A etapa **8. Buscador Próprio** é dedicada ao domínio de busca do ecossistema. O foco técnico-operacional inclui:

- Indexação  
- Busca  
- Filtros  
- Snippets  
- Relevância  
- ACL  
- Integração com fontes internas e PSE  
- Operação na rede da escola  
- Documentação completa das 12 tarefas (18.1–18.12)  

### 2.2 Decisões de produto já fechadas (entrada do escopo)

| Tema | Decisão |
|------|---------|
| Experiência | Tipo Google, voltada a escolas |
| Disponibilidade | Aberto na rede da escola como mecanismo de busca |
| Arquitetura de busca | Índice interno (PostgreSQL FTS + ACL); PSE só para web pública |
| Backend | Kotlin + Spring Boot 3 |
| Frontend | React + Vite + TypeScript |
| Banco | PostgreSQL 16 (FTS + GIN) |
| Auth MVP | JWT próprio; visitante sem login vê `public`/`network` |
| Forma de entrega docs | Markdown por tarefa em `docs/18.x-*` |
| Abordagem | Monólito escolar (API + SPA + Postgres) — MVP sustentável |

### 2.3 O que “aberto na rede” significa neste escopo

- Qualquer dispositivo autenticado na **rede da escola** pode abrir a URL do ByBusca e buscar o catálogo aberto (`public` / `network`)  
- Não exige login para a busca básica  
- Login **amplia** resultados (turma, disciplina, papel)  
- Não significa publicar o acervo privado na internet aberta  

---

## 3. Problema, oportunidade e escopo do problema

### 3.1 Problema

| ID | Problema |
|----|----------|
| P1 | Conteúdos escolares estão fragmentados em vários sistemas |
| P2 | Não há busca unificada com filtros e snippets no padrão esperado pelos usuários |
| P3 | Buscadores públicos (Google web) não respeitam ACL escolar |
| P4 | Indexar material privado em motor externo gera risco de vazamento/privacidade |
| P5 | TI escolar precisa de algo instalável/operável na própria rede |

### 3.2 Oportunidade

Entregar um **Buscador Próprio** que seja:

- Familiar (UX tipo Google)  
- Seguro (ACL no índice interno)  
- Operável (deploy na rede + docs de operação)  
- Extensível (API de indexação para o ecossistema)  
- Completo na etapa (documentação 18.1–18.12 + scaffold)  

### 3.3 Fronteira do problema (o que 18.1 NÃO resolve sozinha)

A 18.1 define o escopo; a realização detalhada ocorre nas tarefas seguintes:

- Menus/perfis → 18.2  
- Regras/fluxos → 18.3  
- UX → 18.4  
- Dados → 18.5  
- Backend → 18.6  
- Frontend → 18.7  
- Deploy → 18.8  
- Manual → 18.9  
- Testes/aceite → 18.10  
- Repositório/IA → 18.11  
- Benchmark → 18.12  

---

## 4. Escopo incluso (executável — MVP da etapa)

Tudo abaixo é **obrigatório** para considerar o MVP da etapa 8 entregue.

### 4.1 Produto / experiência

| ID | Item incluso | Descrição executável |
|----|--------------|----------------------|
| IN-01 | Home tipo Google | Marca **ByBusca** em evidência + campo único + frase de apoio |
| IN-02 | Resultados | Lista com título, tipo, URL, snippet com destaque de termos |
| IN-03 | Filtros por tipo | Abas/fonte: Tudo, Acervo, Aulas, Arquivos, Páginas, Módulos, Web |
| IN-04 | Sugestões | Endpoint/fluxo de autocomplete básico por título |
| IN-05 | Modo visitante | Busca sem login para `public` e `network` |
| IN-06 | Modo logado | JWT amplia resultados `restricted` conforme ACL |
| IN-07 | Painel admin mínimo | Status do índice; orientação PSE; acesso por papel admin/coord |

### 4.2 Domínio de indexação e busca

| ID | Item incluso | Descrição executável |
|----|--------------|----------------------|
| IN-08 | Tipos de documento | `CONTENT`, `COLLECTION`, `LESSON`, `FILE`, `PAGE`, `MODULE` (+ `WEB` via PSE) |
| IN-09 | Visibilidades | `public`, `network`, `restricted` |
| IN-10 | Índice interno | PostgreSQL Full-Text Search (`tsvector` + índice GIN) |
| IN-11 | Relevância | Ranking FTS (`ts_rank_cd`) + merge com resultados PSE |
| IN-12 | Snippets | Trecho ≤ 240 caracteres com destaque dos termos |
| IN-13 | ACL | Filtro por tenant, papel, turma (`class_ids`), disciplina (`subject_ids`) |
| IN-14 | Regra anti-vazamento | Documento sem permissão **omitido** da lista (não 403 item a item) |
| IN-15 | Indexação idempotente | Upsert por (`tenant_id`, `external_id`) |
| IN-16 | Auditoria de busca | Registro de query, usuário (se houver), IP, latência, fontes |

### 4.3 Integrações

| ID | Item incluso | Descrição executável |
|----|--------------|----------------------|
| IN-17 | Google PSE | Somente web pública; secrets só no backend; opcional no MVP |
| IN-18 | Degradação PSE | Se PSE falhar/timeout/quota, busca interna continua + `warnings` |
| IN-19 | API de ingestão | `POST /api/v1/index/documents` para o ecossistema/admin |
| IN-20 | APIs de consulta | `GET /api/v1/search`, `GET /api/v1/suggest`, `POST /api/v1/auth/login` |

### 4.4 Plataforma técnica (scaffold)

| ID | Item incluso | Descrição executável |
|----|--------------|----------------------|
| IN-21 | Backend | `apps/api` — Kotlin + Spring Boot 3 |
| IN-22 | Frontend | `apps/web` — React + Vite + TypeScript |
| IN-23 | Banco | PostgreSQL via Docker Compose + Flyway |
| IN-24 | Config | `.env.example` sem secrets reais |
| IN-25 | Seed demo | Tenant `demo-escola`, usuários e documentos de exemplo |

### 4.5 Documentação da etapa

| ID | Item incluso | Descrição executável |
|----|--------------|----------------------|
| IN-26 | Pacote 18.1–18.12 | Markdown em `docs/18.x-*/README.md` |
| IN-27 | Spec de design | `docs/superpowers/specs/2026-07-24-bybusca-design.md` |
| IN-28 | Plano técnico | `docs/superpowers/plans/2026-07-24-bybusca-implementation.md` |
| IN-29 | README raiz | Como subir ambiente e mapa das tarefas |

---

## 5. Escopo excluído (explícito — fora do MVP)

Itens abaixo **não** fazem parte desta etapa. Evitam escopo infinito e expectativas incorretas.

| ID | Item excluído | Motivo / destino |
|----|---------------|------------------|
| EX-01 | App mobile nativo (Flutter/RN) | Fora do MVP; web responsiva cobre rede escolar |
| EX-02 | OCR / transcrição de áudio-vídeo | Complexidade alta; roadmap futuro |
| EX-03 | Ranking por IA generativa | Custo/risco; FTS + PSE bastam no MVP |
| EX-04 | SaaS multi-rede global | MVP = instalação por escola/rede |
| EX-05 | Substituir o Google na internet inteira | PSE é complementar, não substituto total |
| EX-06 | Indexar conteúdo privado no Google | Vedado por segurança/privacidade |
| EX-07 | SSO/OIDC corporativo (Keycloak/Azure AD) | JWT próprio no MVP; SSO no roadmap |
| EX-08 | Cache distribuído / Elastic/OpenSearch | Postgres FTS suficiente para MVP |
| EX-09 | App de sync offline | Não necessário para busca na rede |
| EX-10 | Moderação editorial avançada / workflow de publicação | Fora do núcleo busca |
| EX-11 | Relatórios analíticos avançados (BI) | Auditoria básica apenas |
| EX-12 | Multilíngue completo (UI i18n) | PT-BR no MVP |

---

## 6. Objetivos mensuráveis e critérios de sucesso

### 6.1 Objetivos (OKR da etapa)

| ID | Objetivo | Indicador (KPI) | Meta MVP | Evidência |
|----|----------|-----------------|----------|-----------|
| O1 | Busca interna funcional | Latência p95 local | < 2s para query típica no seed | Log/`tookMs` + teste manual |
| O2 | ACL correta | Vazamentos em suíte por papel | 0 | Casos CT de 18.10 |
| O3 | Resiliência PSE | Busca interna com PSE down | 100% disponível | Warning `PSE_UNAVAILABLE` sem 5xx |
| O4 | UX tipo Google | Home com marca + campo + resultados | Entregue | Screenshot / protótipo `apps/web` |
| O5 | Operável na rede | URL do frontend como search da escola | Documentado + testável em LAN | 18.8 + smoke test |
| O6 | Indexação confiável | Upsert sem duplicar `external_id` | Idempotente | Teste de reindex |
| O7 | Documentação completa | 12 tarefas 18.x presentes | 12/12 | Pastas `docs/18.*` |
| O8 | Scaffold executável | API compila; web builda | Build OK | `gradlew compileKotlin` + `npm run build` |

### 6.2 Definição de “Pronto” da etapa 8 (visão 18.1)

A etapa está pronta para aceite do supervisor quando:

1. Escopo 18.1 aprovado  
2. Demais documentos 18.2–18.12 coerentes com este escopo  
3. Scaffold sobe com Postgres e permite busca no seed  
4. Checklist de aceite MVP (18.10) passa nos itens críticos de ACL e busca  

---

## 7. Personas, atores e jornada resumida

### 7.1 Personas

| Persona | Necessidade principal | O que o ByBusca entrega |
|---------|----------------------|-------------------------|
| **Visitante na rede** | Buscar rápido sem login | Catálogo `public`/`network` + web PSE |
| **Aluno** | Material da turma | Login amplia para `restricted` da turma |
| **Professor** | Material da disciplina + planos | ACL por disciplina/papel docente |
| **Coordenação** | Visão ampla + auditoria | Papel COORDINATOR; health do índice |
| **Admin / TI** | Operar, configurar, recuperar | Painel, env, deploy, backup (18.8/18.9) |
| **Supervisor do projeto** | Aceite da etapa | Pacote documental + evidências |

### 7.2 Jornadas mínimas cobertas pelo escopo

1. **Busca aberta:** abre ByBusca → digita → vê resultados escolares abertos (+ web se habilitada)  
2. **Busca autenticada:** entra → busca → vê também materiais da turma/disciplina  
3. **Indexação:** admin/sistema envia documento → passa a aparecer na busca  
4. **Falha PSE:** web some com aviso; busca interna segue  

---

## 8. Escopo técnico resumido (para execução)

### 8.1 Arquitetura-alvo (vinculada ao escopo)

```text
Browser (rede da escola)
   → React/Vite (ByBusca UI)
      → Spring Boot API
           → PostgreSQL FTS + ACL + auditoria
           → Google PSE (somente web pública, opcional)
```

### 8.2 Stack e versões-alvo

| Camada | Tecnologia | Versão-alvo |
|--------|------------|-------------|
| Backend | Kotlin + Spring Boot | 2.x / 3.3.x |
| JDK | Temurin/Oracle | 17+ (21 recomendado) |
| Frontend | React + Vite + TS | 18 / 5 / 5 |
| Banco | PostgreSQL | 16 |
| Migrações | Flyway | via Spring Boot |
| Busca web | Google Custom Search JSON API | v1 |
| Container DB | Docker Compose | v2 |

### 8.3 Princípios de escopo técnico (não negociáveis no MVP)

1. Conteúdo privado **nunca** vai para o PSE/Google  
2. ACL é aplicada **antes** da resposta ao usuário  
3. Busca aberta na rede é o comportamento padrão  
4. Falha externa não pode derrubar o núcleo escolar  
5. Secrets fora do frontend e fora do Git  

---

## 9. Premissas

| ID | Premissa |
|----|----------|
| PR-01 | Existe rede escolar onde o ByBusca pode ser publicado como URL de busca |
| PR-02 | A escola/rede aceita busca aberta do catálogo `public`/`network` na LAN |
| PR-03 | Haverá (quando desejado) credenciais PSE (`API key` + `cx`) fornecidas pela operação |
| PR-04 | Módulos do ecossistema podem enviar documentos via API de indexação (ou carga admin) |
| PR-05 | Ambiente de build possui JDK 17+, Node 20+ e Docker (ou Postgres equivalente) |
| PR-06 | Dados de demonstração são sintéticos (sem dados reais de alunos em seed) |
| PR-07 | Supervisor valida o pacote documental da etapa no cadastro |
| PR-08 | Idioma oficial da entrega documental e UI MVP: **português (Brasil)** |

---

## 10. Restrições e dependências

### 10.1 Restrições

| ID | Restrição |
|----|-----------|
| RE-01 | MVP não depende de Elastic/OpenSearch/Meilisearch |
| RE-02 | Auth SSO fica fora; apenas JWT próprio |
| RE-03 | Um tenant por instalação no MVP (`demo-escola` / escola da rede) |
| RE-04 | PSE sujeito a quota/custo Google — deve ser desligável |
| RE-05 | Conformidade básica LGPD: auditoria com retenção definida (detalhe em 18.5) |
| RE-06 | Sem commits de secrets (`.env` real fora do repositório) |

### 10.2 Dependências

| Dependência | Tipo | Impacto se faltar |
|-------------|------|-------------------|
| PostgreSQL | Técnica | API não sobe |
| Credenciais PSE | Opcional | Só a aba/fonte Web fica degradada |
| Conteúdos para indexar | Negócio | Busca interna limitada ao seed |
| DNS/proxy da escola | Operação | Não vira “mecanismo padrão” da rede |
| Aprovação do supervisor | Gestão | Etapa não fecha no cadastro |

---

## 11. Riscos, impactos e mitigações

| ID | Risco | Prob. | Impacto | Mitigação no escopo |
|----|-------|-------|---------|---------------------|
| RK-01 | Quota/falha PSE | Média | Médio | Circuit breaker + warning; núcleo interno independente |
| RK-02 | ACL incorreta / vazamento | Baixa/Média | **Alto** | Testes por papel; omitir itens; revisão em 18.10 |
| RK-03 | Expectativa “igual Google mundial” | Alta | Médio | Comunicar PSE complementar (18.9/18.12) |
| RK-04 | Índice desatualizado | Média | Médio | API upsert + reindex admin |
| RK-05 | Ambiente sem Docker/JDK | Média | Médio | Documentar alternativas em 18.8; seed SQL versionado |
| RK-06 | Uso indevido de IA com dados sensíveis | Baixa | Alto | Política em 18.11; seed sintético |
| RK-07 | Escopo creeping (pedir mobile/IA/OCR) | Alta | Alto | Lista EX-* explícita neste 18.1 |

---

## 12. Entregáveis da tarefa 18.1 e da etapa

### 12.1 Entregáveis específicos da 18.1

1. Este documento de **Escopo executável e objetivo** (versão detalhada)  
2. Alinhamento explícito com a spec de design aprovada  
3. Matriz incluso/excluído, métricas, personas, premissas, riscos  
4. Critérios de conclusão da própria 18.1  

### 12.2 Entregáveis da etapa 8 (visão consolidada — cobertos pelo escopo)

| Pacote | Local |
|--------|-------|
| Docs 18.1–18.12 | `docs/18.*` |
| Spec | `docs/superpowers/specs/2026-07-24-bybusca-design.md` |
| Plano | `docs/superpowers/plans/2026-07-24-bybusca-implementation.md` |
| API | `apps/api` |
| Web | `apps/web` |
| Infra local | `docker-compose.yml`, `.env.example`, `README.md` |

### 12.3 Não-entregáveis (reforço)

- Produção com dados reais de alunos sem processo LGPD da escola  
- Configuração real de PSE no repositório (apenas placeholders)  
- Homologação formal assinada (fica no processo do cadastro/supervisor)

---

## 13. Cronograma lógico da etapa (ordem de execução)

Ordem recomendada (já refletida na numeração 18.x):

1. **18.1** Escopo (este documento)  
2. **18.2** Menu, módulos, perfis, permissões  
3. **18.3** Regras, fluxos, integrações  
4. **18.4** UX / telas / protótipo  
5. **18.5** Banco / entidades / rastreabilidade  
6. **18.6** Backend / APIs / segurança  
7. **18.7** Frontend / painéis  
8. **18.8** Instalação / deploy / recuperação  
9. **18.9** Manual de uso / operação / suporte  
10. **18.10** Testes / homologação / aceite / MVP  
11. **18.11** Repositórios / versionamento / IA / docs  
12. **18.12** Referências / benchmark / comparação  

> Observação: documentação e scaffold podem avançar em paralelo após o fechamento do 18.1, desde que não violem IN/EX deste escopo.

---

## 14. Critérios de aceite da tarefa 18.1

A tarefa **18.1** é aceita quando o supervisor puder marcar “sim” em todos os itens:

- [ ] Objetivo geral e específico estão claros e mensuráveis  
- [ ] Escopo incluso (IN-*) está completo o bastante para executar a etapa  
- [ ] Escopo excluído (EX-*) está explícito e evita ambiguidades  
- [ ] Personas e jornadas mínimas estão descritas  
- [ ] Premissas, restrições, dependências e riscos estão registrados  
- [ ] Entregáveis e ordem 18.2–18.12 estão mapeados  
- [ ] Documento está em Markdown no repositório (`docs/18.1-escopo`)  
- [ ] Não há contradição com a arquitetura A (Postgres+ACL interno; PSE só web)  

---

## 15. Critério de conclusão da 18.1

Com o conteúdo das seções 1–14 preenchido, revisado e alinhado ao design aprovado (**Abordagem 1 — monólito escolar** + arquitetura A), a tarefa **18.1 — Escopo executável e objetivo da etapa** considera-se **concluída e pronta para envio no cadastro**.

**Status:** Concluído (documento detalhado v1.1)  
**Próximo passo sugerido no cadastro:** enviar 18.1 e seguir 18.2 (menu, módulos, perfis e permissões).

---

## 16. Anexos / referências internas

- Spec: `docs/superpowers/specs/2026-07-24-bybusca-design.md`  
- Plano: `docs/superpowers/plans/2026-07-24-bybusca-implementation.md`  
- README do produto: `README.md`  
- Benchmark e justificativa da arquitetura: `docs/18.12-referencias-benchmark/README.md`  
