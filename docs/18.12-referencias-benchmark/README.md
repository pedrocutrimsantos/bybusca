# 18.12 — Referências modelo, benchmark e comparação prática

| Campo | Valor |
|-------|-------|
| **Produto** | ByBusca — Buscador Próprio do ecossistema escolar |
| **Etapa** | 8. Buscador Próprio |
| **Tarefa** | 18.12 Referências modelo, benchmark e comparação prática |
| **Status** | Pronto para envio |
| **Supervisor** | Luis Rodrigo Lima Rodrigues |
| **Participante** | Pedro Cutrim |
| **Versão** | 1.1 |

---

## 1. Objetivo

Registrar os **modelos de referência**, o **benchmark prático** e a **decisão de arquitetura** do ByBusca frente a alternativas (Google Web, PSE puro, Elastic/Meili, LMS search), justificando a opção adotada na etapa 8.

---

## 2. Modelos de referência

| Modelo | O que inspira no ByBusca | O que **não** copiamos |
|--------|--------------------------|-------------------------|
| Google Search | Campo único, snippets, velocidade percebida, marca hero | Índice web global, ads, tracking agressivo |
| Google Programmable Search | Busca web configurável (`cx=e54b7047fc7e64e10`), SafeSearch, exclusão de sites | Usar PSE como índice do acervo privado |
| Moodle Global Search | Contexto educacional / LMS | UX datada; acoplamento forte ao LMS |
| Microsoft Search / SharePoint | ACL corporativa por perfil | Custo/complexidade de licenciamento |
| Meilisearch / Elasticsearch | Relevância, facets, escala | Ops extras desnecessárias no MVP |
| Wikipedia / SciELO (fontes) | Prioridade educacional/científica na web | — |

---

## 3. Requisitos que o benchmark precisa atender

| ID | Requisito escolar |
|----|-------------------|
| R1 | UX tipo Google, aberta na rede |
| R2 | ACL fina (turma/disciplina/papel) |
| R3 | Acervo privado **não** vaza para a internet |
| R4 | Web pública filtrada (sem adulto/apostas/streaming impróprio) |
| R5 | MVP operável com TI escolar (baixo custo ops) |
| R6 | Documentável e entregável na etapa 8 |

---

## 4. Benchmark prático (MVP)

Escala: **Fraca / Parcial / Boa / Forte / Excelente / N/A**

| Critério | Google Web | PSE puro | Elastic/Meili + UI | Moodle Search | **ByBusca (A)** |
|----------|------------|----------|--------------------|---------------|-----------------|
| UX tipo Google (R1) | Excelente | Boa | Parcial–Boa | Fraca | **Boa** |
| ACL escolar fina (R2) | Fraca | Fraca | Boa | Parcial | **Forte** |
| Privacidade acervo (R3) | N/A | Ruim se indexar privado | Boa | Boa | **Forte** |
| Web pública (R4) | Nativa | Nativa + filtros CSE | Via conector | Fraca | **Boa (PSE+filtros)** |
| Custo ops MVP (R5) | — | Baixo | Médio–Alto | Já no LMS | **Baixo (Postgres)** |
| Adequação etapa 8 (R6) | Parcial | Parcial | Parcial | Parcial | **Alta** |
| SafeSearch / exclusões | Parcial | Boa (painel) | Manual | N/A | **Boa (código+painel)** |

---

## 5. Arquiteturas avaliadas (decisão)

### 5.1 Opção A — escolhida

**Postgres FTS + ACL interno** como motor do acervo escolar; **Google PSE/CSE** só para web pública, com SafeSearch e lista de exclusão.

| Prós | Contras |
|------|---------|
| ACL correta; privado nunca vai ao Google | FTS Postgres &lt; Elastic em escala massiva |
| Ops simples (um Postgres) | Web depende de CSE/config Google |
| MVP rápido; docs coerentes | — |

### 5.2 Opção B — rejeitada

**PSE como motor principal** + pós-filtro ACL.

| Problema |
|----------|
| Metadados/títulos podem vazar antes do filtro |
| ACL frágil para material escolar sensível |
| Viola RN05 / princípio de privacidade |

### 5.3 Opção C — rejeitada

Indexação/ACL 100% no backend; PSE só como “ranker” do privado.

| Problema |
|----------|
| PSE não ranqueia bem o que não indexa |
| Complexidade sem ganho real |

---

## 6. Comparação prática com o entregável atual

| Capacidade | Evidência no ByBusca |
|------------|----------------------|
| Busca interna | `SearchFacade` + `portuguese_unaccent` |
| ACL | `AclService` + seed aluno/prof |
| Web filtrada | `GoogleCsePanel` + `safeSearch.ts` + `SITES-EXCLUIDOS.md` |
| Engine | CX `e54b7047fc7e64e10` |
| Admin | health + reindex |
| Docs etapa | `docs/18.1` … `docs/18.12` |

URL pública do mecanismo web:  
https://cse.google.com/cse?cx=e54b7047fc7e64e10

---

## 7. Riscos residuais e mitigação

| Risco | Mitigação |
|-------|-----------|
| Novo site impróprio na web | Atualizar lista CSE + denylist mensalmente |
| Expectativa “igual Google mundial” | Comunicar: web é complementar e filtrada (18.9) |
| Escala FTS | Monitorar; avaliar Meili/Elastic só se volume exigir |
| Quota PSE JSON | Widget CSE cobre web sem API key no MVP |

---

## 8. Conclusão

Para escola/rede com busca aberta, materiais sensíveis e necessidade de web educacional segura, a **Arquitetura A (ByBusca)** é a melhor escolha da etapa 8:

- experiência tipo Google  
- ACL forte no acervo  
- PSE/CSE só na web pública, com SafeSearch e exclusões  
- custo operacional compatível com TI escolar  
- pacote documental completo para envio no cadastro  

---

## 9. Critérios de aceite da 18.12

- [ ] Referências e não-objetivos claros  
- [ ] Tabela de benchmark preenchida  
- [ ] Opções A/B/C com decisão justificada  
- [ ] Ligação ao entregável real (CX, filtros, stack)  

## 10. Critério de conclusão

Benchmark e decisão arquitetural registrados → **18.12 concluída / pronta para envio**.

---

## 11. Encerramento da etapa 8 (visão consolidada)

Com **18.1–18.12** detalhados e o scaffold operacional, a etapa **8. Buscador Próprio** está apta a:

1. Envio das 12 tarefas no cadastro  
2. Homologação (checklist 18.10)  
3. Aceite do supervisor Luis Rodrigo Lima Rodrigues  
