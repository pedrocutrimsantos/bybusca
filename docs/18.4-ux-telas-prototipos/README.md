# 18.4 — UX, telas e protótipos funcionais

| Campo | Valor |
|-------|-------|
| **Produto** | ByBusca — Buscador Próprio do ecossistema escolar |
| **Etapa** | 8. Buscador Próprio |
| **Tarefa** | 18.4 UX, telas e protótipos funcionais |
| **Status** | Pronto para envio |
| **Supervisor** | Luis Rodrigo Lima Rodrigues |
| **Participante** | Pedro Cutrim |
| **Versão** | 1.1 |
| **Depende de** | 18.1, 18.2 |
| **Alimenta** | 18.7 (frontend), 18.9 (manual), 18.10 (aceite visual) |

---

## 1. Objetivo da tarefa

Definir a **experiência de uso** do ByBusca (tipo Google escolar), mapear **telas MVP**, **wireframes**, **microinterações**, **acessibilidade** e o **protótipo funcional** em `apps/web`.

---

## 2. Direção de UX / visual

### 2.1 Princípios

| ID | Princípio |
|----|-----------|
| UX01 | Primeiro viewport = **uma composição**: marca + campo + uma frase |
| UX02 | Marca **ByBusca** é sinal hero (não só texto de nav) |
| UX03 | Sem dashboard na home; sem cards no hero |
| UX04 | Um job por seção (buscar → ver resultados → abrir recurso) |
| UX05 | Tipografia expressiva (Fraunces + Source Sans 3) |
| UX06 | Fundo com atmosfera (gradientes verdes/creme escolares) — sem “AI purple” |
| UX07 | Motion só para hierarquia (rise, fade, skeleton) — sem ruído |

### 2.2 Tokens visuais (implementados)

| Token | Uso | Valor de referência |
|-------|-----|---------------------|
| `--ink` | Texto principal | `#1c2b24` |
| `--muted` | Apoio | `#4d5c54` |
| `--accent` | CTA / links | `#1f6b4a` |
| `--accent-2` | Tipo/meta | `#c45c26` |
| `--bg1/--bg2` | Gradiente de página | `#e8f1ea` → `#f7f3e8` |
| Display | Marca/títulos | Fraunces |
| Body | UI/resultados | Source Sans 3 |

### 2.3 Anti-padrões (evitar)

- Hero com stats, agenda, cards flutuantes, badges sobre imagem  
- Pills demais / glow / dark mode forçado  
- Campo de busca secundário escondido no menu  

---

## 3. Mapa de telas MVP

| ID | Tela | Rota | Persona principal |
|----|------|------|-------------------|
| A | Home de busca | `/` | Visitante / todos |
| B | Resultados | `/?q=...&type=...` | Todos |
| C | Login | `/login` | Aluno/Prof/Admin |
| D | Admin | `/admin` | COORD / ADMIN |
| E | Ajuda | `/ajuda` | Todos |

---

## 4. Especificação por tela

### 4.1 Tela A — Home (`/`)

**Objetivo:** iniciar busca em < 3 segundos mentais.

| Elemento | Spec |
|----------|------|
| Marca | `ByBusca` grande, central |
| Tagline | “Busque aulas, acervo, arquivos e a web da escola” |
| Campo | Único, autofoco, Enter dispara busca |
| Suggest | Dropdown após ≥2 chars |
| Topbar | Entrar / Admin / Ajuda (e Sair se logado) |

**Estados:** idle · digitando · suggest aberto.

### 4.2 Tela B — Resultados (`/?q=`)

| Elemento | Spec |
|----------|------|
| Campo | Persistente no topo (valor = `q`) |
| Filtros | Tudo · Acervo · Aulas · Arquivos · Páginas · Módulos · Web · **Imagens** |
| Meta | “Cerca de N resultados (X ms)” |
| Item | tipo · título (link) · snippet com `<mark>` · URL |
| Warning PSE | faixa âmbar não bloqueante |
| Vazio | “Nenhum resultado para …” |
| Erro | mensagem amigável (sem stack) |
| Loading | 3 skeletons |

**Aba Imagens (`type=IMAGE`)**

| Elemento | Spec |
|----------|------|
| Texto | Reusa `q` da URL; Enter → grade de thumbnails (sites allowlist) |
| Upload | Botão/arquivo JPG·PNG·WebP, máx. 5 MB |
| URL | Campo + enviar → busca por foto |
| Grade | Thumb, título, link da página (`target=_blank`) |
| Estados | loading texto / loading foto / vazio / aviso amigável (`PSE_UNAVAILABLE`, `VISION_UNAVAILABLE`, `IMAGE_BLOCKED`) |
| Acervo interno | Não listado nesta aba (só web de imagens) |

**Wireframe**

```text
┌──────────────────────────────────────────────────┐
│ ByBusca   [=========== buscar ============] [🔍] │
│ Tudo · Acervo · Aulas · Arquivos · Páginas · Web · Imagens │
│ Cerca de 2 resultados (12 ms)                    │
├──────────────────────────────────────────────────┤
│ Aula                                             │
│ Equações do 1º grau                              │
│ ... introdução às <mark>equações</mark> ...      │
│ https://escola.local/aulas/mat-01                │
└──────────────────────────────────────────────────┘
```

### 4.3 Tela C — Login (`/login`)

| Elemento | Spec |
|----------|------|
| Campos | usuário + senha |
| CTA | Entrar |
| Hint demo | admin/prof/aluno · `senha123` |
| Sucesso | sessionStorage token + redirect `/` |
| Erro | “Usuário ou senha inválidos” |

### 4.4 Tela D — Admin (`/admin`)

| Elemento | Spec |
|----------|------|
| Stats | tenant, documentos ativos, status |
| Ação | botão **Reindexar tenant** |
| Feedback | “Reindexados N documentos em X ms” |
| Hint PSE | secrets só via env (sem form de key no browser) |
| Sem auth | mensagem pedindo login admin |

### 4.5 Tela E — Ajuda (`/ajuda`)

| Elemento | Spec |
|----------|------|
| Como buscar | passos curtos |
| Login | quando usar |
| Contatos | placeholders da escola |
| Links | docs 18.9 |

---

## 5. Microinterações (mínimo 3)

| # | Interação | Onde |
|---|-----------|------|
| 1 | Autofoco + rise na home | Tela A |
| 2 | Skeleton shimmer no loading | Tela B |
| 3 | Highlight `<mark>` nos termos | Snippet |
| 4 | Suggest fade-in | Campo |
| 5 | Focus ring no campo (elevação leve) | SearchBox |

---

## 6. Acessibilidade

| Requisito | Como |
|-----------|------|
| Nome do campo | `<label class="sr-only">` + `id="q"` |
| Teclado | Tab / Enter / Esc (fecha suggest no blur) |
| Contraste | texto `--ink`/`--muted` sobre fundos claros |
| Highlight | `<mark>` com fundo âmbar (não só cor de texto) |
| Links | foco visível do browser + underline no hover |
| Status | `aria-busy` nos skeletons; `aria-expanded` no suggest |

---

## 7. Responsividade

| Breakpoint | Comportamento |
|------------|---------------|
| ≥640px | campo em pílula horizontal |
| <640px | campo empilhado; botão Buscar full-width; filtros wrap |

---

## 8. Protótipo funcional (código)

| Peça | Arquivo |
|------|---------|
| Home/Resultados | `apps/web/src/pages/HomePage.tsx` |
| Login | `apps/web/src/pages/LoginPage.tsx` |
| Admin | `apps/web/src/pages/AdminPage.tsx` |
| Ajuda | `apps/web/src/pages/HelpPage.tsx` |
| SearchBox + suggest | `apps/web/src/components/SearchBox.tsx` |
| Imagens (texto + foto) | `apps/web/src/components/ImageSearchPanel.tsx` |
| Estilos | `apps/web/src/styles/global.css` |

APIs consumidas: `/search`, `/suggest`, `/search/images`, `/search/image`, `/auth/login`, `/admin/health-index`, `/index/reindex`.

---

## 9. Critérios de aceite da 18.4

- [ ] Home com marca hero + campo único (sem dashboard)  
- [ ] Resultados com filtros, snippet marcado e warning PSE  
- [ ] Login e Admin utilizáveis no protótipo  
- [ ] Ajuda acessível pelo menu  
- [ ] ≥3 microinterações presentes  
- [ ] Uso básico por teclado possível  

## 10. Critério de conclusão

Direção UX, telas, wireframes e protótipo documentados e refletidos em `apps/web` → **18.4 concluída / pronta para envio**.
