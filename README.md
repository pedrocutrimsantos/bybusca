# ByBusca

Mecanismo de busca da rede escolar (experiência tipo Google): índice interno com ACL no PostgreSQL + Google Programmable Search para web pública.

**Versão:** 0.1.0-MVP  
**Stack:** Kotlin · Spring Boot 3.3 · PostgreSQL 16 · React 18 · Vite 5 · TypeScript 5

## Documentação da etapa (pronto para enviar)

| Tarefa | Pasta | Detalhe |
|--------|-------|---------|
| 18.1 Escopo | [docs/18.1-escopo](docs/18.1-escopo) | v1.1 |
| 18.2 Menu, perfis, permissões | [docs/18.2-menu-perfis-permissoes](docs/18.2-menu-perfis-permissoes) | v1.1 |
| 18.3 Regras e integrações | [docs/18.3-regras-fluxos-integracoes](docs/18.3-regras-fluxos-integracoes) | v1.1 (+ sites excluídos) |
| 18.4 UX e protótipos | [docs/18.4-ux-telas-prototipos](docs/18.4-ux-telas-prototipos) | v1.1 |
| 18.5 Banco e rastreabilidade | [docs/18.5-banco-entidades-rastreabilidade](docs/18.5-banco-entidades-rastreabilidade) | v1.1 |
| 18.6 Backend e segurança | [docs/18.6-backend-apis-seguranca](docs/18.6-backend-apis-seguranca) | v1.1 |
| 18.7 Frontend e painéis | [docs/18.7-frontend-paineis](docs/18.7-frontend-paineis) | v1.1 |
| 18.8 Install, deploy, DR | [docs/18.8-instalacao-deploy-recuperacao](docs/18.8-instalacao-deploy-recuperacao) | v1.1 |
| 18.9 Manual | [docs/18.9-manual-uso-operacao-suporte](docs/18.9-manual-uso-operacao-suporte) | v1.1 |
| 18.10 Testes e MVP | [docs/18.10-testes-homologacao-mvp](docs/18.10-testes-homologacao-mvp) | v1.1 |
| 18.11 Repos e IA | [docs/18.11-repositorios-versionamento-ia](docs/18.11-repositorios-versionamento-ia) | v1.1 |
| 18.12 Benchmark | [docs/18.12-referencias-benchmark](docs/18.12-referencias-benchmark) | v1.1 |

Spec: [docs/superpowers/specs/2026-07-24-bybusca-design.md](docs/superpowers/specs/2026-07-24-bybusca-design.md)

## Banco local (sem Docker)

Este ambiente detectou PostgreSQL instalado. Crie o database uma vez:

```powershell
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -h localhost -f scripts\setup-db.sql
```

Depois:

```powershell
cd apps\api ; .\gradlew.bat bootRun
cd apps\web ; npm run dev
```

Health: http://localhost:8080/api/v1/health  
Busca: http://localhost:8080/api/v1/search?q=equações  
Web: http://localhost:5173


## PSE (busca web)

| Campo | Valor |
|-------|-------|
| ID do mecanismo (`cx`) | `e54b7047fc7e64e10` |
| URL pública | https://cse.google.com/cse?cx=e54b7047fc7e64e10 |

No ByBusca o widget CSE usa esse `cx` (aba **Web**). Para a JSON API no backend:

```text
GOOGLE_PSE_ENABLED=true
GOOGLE_PSE_API_KEY=...
GOOGLE_PSE_CX=e54b7047fc7e64e10
VITE_GOOGLE_PSE_CX=e54b7047fc7e64e10
```

Sem API key, a busca **interna** e o **widget web** continuam funcionando.
