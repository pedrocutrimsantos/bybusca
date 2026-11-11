# 18.11 — Repositórios, versionamento, uso de IA e documentação

| Campo | Valor |
|-------|-------|
| **Produto** | ByBusca — Buscador Próprio do ecossistema escolar |
| **Etapa** | 8. Buscador Próprio |
| **Tarefa** | 18.11 Repositórios, versionamento, uso de IA e documentação |
| **Status** | Pronto para envio |
| **Supervisor** | Luis Rodrigo Lima Rodrigues |
| **Participante** | Pedro Cutrim |
| **Versão** | 1.1 |

---

## 1. Objetivo

Definir a governança do código e da documentação: estrutura do repositório, branches, SemVer, o que não versionar, política de uso de IA e mapa dos artefatos vivos da etapa.

---

## 2. Repositório

| Item | Valor |
|------|-------|
| Nome | `ByBusca` |
| Modelo | Monorepo |
| Branch principal | `main` |
| Branches de trabalho | `feature/…`, `fix/…`, `docs/…` |

### 2.1 Árvore principal

```text
ByBusca/
├── apps/
│   ├── api/          # Kotlin + Spring Boot
│   └── web/          # React + Vite + TS
├── docs/
│   ├── 18.1 … 18.12  # entregáveis da etapa
│   └── superpowers/  # spec + plano
├── scripts/          # setup-db.sql / ps1
├── docker-compose.yml
├── .env.example
├── .gitignore
└── README.md
```

---

## 3. Versionamento

| Tema | Política |
|------|----------|
| SemVer produto | `0.1.0-MVP` (bootstrap) |
| Tags | `v0.1.0`, `v0.1.1`, … |
| Commits | Mensagem clara (por quê); idioma consistente (PT ou EN) |
| Releases | Tag + notas curtas (o que mudou para a escola) |
| Flyway | Nunca editar migração já aplicada; criar `Vn+1` |

### 3.1 O que **não** versionar

| Item | Motivo |
|------|--------|
| `.env` / `.env.local` | secrets |
| `GOOGLE_PSE_API_KEY` | segredo |
| dumps Postgres com dados reais | LGPD |
| `node_modules/`, `build/`, `.gradle/` | gerados |
| chave JWT de produção | segredo |

**Pode versionar:** `GOOGLE_PSE_CX` (ID público do engine), listas de exclusão, docs, seed sintético.

---

## 4. Fluxo de trabalho Git (sugerido)

```text
main ──► feature/busca-segura ──► PR/revisão ──► merge main ──► tag
```

1. Branch a partir de `main`  
2. Commits pequenos e revisáveis  
3. Revisar ACL/segurança em PRs sensíveis  
4. Atualizar doc 18.x se mudar contrato  
5. Tag após aceite de marco  

---

## 5. Uso de IA (esta etapa e além)

| Uso | Permitido? | Condição |
|-----|:----------:|----------|
| Docs 18.x, spec, planos | Sim | Revisão humana antes de “Enviar” |
| Scaffold / boilerplate | Sim | Revisar ACL, auth, secrets |
| Refatoração assistida | Sim | Rodar testes/smoke |
| Colar secrets / API keys | **Não** | — |
| Dados pessoais de alunos | **Não** | Usar seed sintético |
| Gerar exploits / bypass ACL | **Não** | — |

**Regra de ouro:** toda saída de IA passa por revisão do participante (Pedro) e, no aceite, do supervisor (Luis Rodrigo).

Registro sugerido no PR/cadastro: “Trechos assistidos por IA: docs 18.x / scaffold; revisados em &lt;data&gt;.”

---

## 6. Documentação viva (mapa)

| Artefato | Caminho |
|----------|---------|
| Spec de design | `docs/superpowers/specs/2026-07-24-bybusca-design.md` |
| Plano de implementação | `docs/superpowers/plans/2026-07-24-bybusca-implementation.md` |
| Escopo | `docs/18.1-escopo` |
| Perfis/ACL | `docs/18.2-menu-perfis-permissoes` |
| Regras/integrações | `docs/18.3-regras-fluxos-integracoes` |
| Sites excluídos CSE | `docs/18.3-…/SITES-EXCLUIDOS.md` + `sites-excluidos-cse.txt` |
| UX | `docs/18.4-ux-telas-prototipos` |
| Banco | `docs/18.5-banco-entidades-rastreabilidade` |
| Backend | `docs/18.6-backend-apis-seguranca` |
| Frontend | `docs/18.7-frontend-paineis` |
| Install/DR | `docs/18.8-instalacao-deploy-recuperacao` |
| Manual | `docs/18.9-manual-uso-operacao-suporte` |
| Testes/MVP | `docs/18.10-testes-homologacao-mvp` |
| Repos/IA | `docs/18.11-repositorios-versionamento-ia` (este) |
| Benchmark | `docs/18.12-referencias-benchmark` |
| README | `README.md` |

---

## 7. Responsabilidades

| Papel | Responsabilidade |
|-------|------------------|
| Pedro Cutrim | Manter repo, docs, scaffold; preparar envio |
| Luis Rodrigo Lima Rodrigues | Aprovar etapa / aceite |
| TI escola | Secrets, deploy, CSE panel, backups |

---

## 8. Critérios de aceite da 18.11

- [ ] Estrutura monorepo e branches definidas  
- [ ] Política do que não versionar explícita  
- [ ] Uso de IA com limites claros  
- [ ] Mapa completo dos artefatos 18.x  

## 9. Critério de conclusão

Governança de repo, versionamento, IA e docs documentada → **18.11 concluída / pronta para envio**.
