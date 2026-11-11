# 18.7 — Frontend, funcionalidades e painéis

| Campo | Valor |
|-------|-------|
| **Produto** | ByBusca — Buscador Próprio do ecossistema escolar |
| **Etapa** | 8. Buscador Próprio |
| **Tarefa** | 18.7 Frontend, funcionalidades e painéis |
| **Status** | Pronto para envio |
| **Supervisor** | Luis Rodrigo Lima Rodrigues |
| **Participante** | Pedro Cutrim |
| **Versão** | 1.1 |
| **Stack** | React 18 · Vite 5 · TypeScript 5 · React Router 6 |
| **Depende de** | 18.4 (UX), 18.6 (APIs) |
| **Alimenta** | 18.9 (manual), 18.10 (aceite UI) |

---

## 1. Objetivo

Especificar o frontend `apps/web`: estrutura, rotas, funcionalidades, integração com API/CSE, estados e painéis — conforme protótipo funcional.

---

## 2. Estrutura de pastas

```text
apps/web/src
├── main.tsx
├── App.tsx                 # rotas
├── api/client.ts           # fetch autenticado
├── config/
│   ├── pse.ts              # CX + URL pública CSE
│   └── safeSearch.ts       # denylist + query educacional
├── components/
│   ├── SearchBox.tsx       # campo + suggest
│   ├── ResultList.tsx      # lista interna + skeletons
│   ├── Filters.tsx         # abas de tipo (incl. Imagens)
│   ├── GoogleCsePanel.tsx  # widget web SafeSearch
│   └── ImageSearchPanel.tsx # texto→imagens + upload/URL foto
├── pages/
│   ├── HomePage.tsx        # home + resultados
│   ├── LoginPage.tsx
│   ├── AdminPage.tsx
│   └── HelpPage.tsx
└── styles/global.css
```

---

## 3. Rotas

| Rota | Página | Auth |
|------|--------|------|
| `/` | Home / Resultados (`?q=&type=`) | opcional |
| `/login` | Login | público |
| `/admin` | Painel admin | ADMIN/COORD (API) |
| `/ajuda` | Ajuda | público |
| `*` | redirect `/` | — |

---

## 4. Funcionalidades MVP

| ID | Função | Onde | Status |
|----|--------|------|--------|
| F1 | Busca Enter/botão | Home | ✓ |
| F2 | Filtros por tipo | Resultados | ✓ |
| F3 | Snippet com `<mark>` | ResultList | ✓ |
| F4 | Web CSE filtrada (SafeSearch + denylist query) | GoogleCsePanel | ✓ |
| F5 | Login JWT → `sessionStorage` | Login | ✓ |
| F6 | Badge usuário + Sair | Header | ✓ |
| F7 | Health índice | Admin | ✓ |
| F8 | Reindex tenant | Admin | ✓ |
| F9 | Suggest autocomplete | SearchBox | ✓ |
| F10 | Estados loading/vazio/erro | Resultados | ✓ |
| F11 | Ajuda + link engine CSE | Ajuda/Admin | ✓ |
| F12 | Exibir ID/URL PSE | Ajuda/Admin | ✓ |
| F13 | Aba **Imagens** (`type=IMAGE`) + grade de thumbs | ImageSearchPanel | ✓ |
| F14 | Busca por foto (upload ou URL) | ImageSearchPanel | ✓ |

---

## 5. Comportamento da busca (Home)

| Aba `type` | Resultados escola (API) | Web (CSE widget) |
|------------|-------------------------|------------------|
| `ALL` | sim | sim (abaixo) |
| `LESSON`…`MODULE` | filtrado por tipo | não |
| `WEB` | não | sim |
| `IMAGE` | não | grade via API (`/search/images` + `/search/image`) |

- Query string é fonte de verdade: `/?q=equacoes&type=ALL` ou `/?q=mapa&type=IMAGE`
- Busca interna **não** envia conteúdo privado ao Google
- Web usa `buildSafeWebQuery()` + `safeSearch: active`

---

## 6. Painéis

### 6.1 Busca (principal)
- Hero: marca **ByBusca** + campo + tagline  
- Resultados: filtros, meta “Escola · N resultados”, lista, painel CSE  
- Aba **Imagens** (`type=IMAGE`): `ImageSearchPanel` — busca textual na grade; zona upload + campo URL para foto→similares; links abrem em nova aba  

### 6.2 Login
- Usuário/senha · hint demo · redirect `/`  

### 6.3 Admin
- Stats tenant/docs/status  
- Botão **Reindexar tenant**  
- Bloco PSE: CX `e54b7047fc7e64e10` + URL pública  

### 6.4 Ajuda
- Como buscar · web filtrada · login · admin · contatos  

---

## 7. Integração HTTP

| Item | Valor |
|------|-------|
| Base | `VITE_API_BASE_URL` → `http://localhost:8080/api/v1` |
| Tenant | `VITE_TENANT` → `demo-escola` |
| CSE CX | `VITE_GOOGLE_PSE_CX` → `e54b7047fc7e64e10` |
| Auth header | `Authorization: Bearer …` se token |
| Token storage | `sessionStorage` (`bybusca_token`, `bybusca_user`) |

Funções em `api/client.ts`: `search`, `suggest`, `login`, `healthIndex`, `reindex`, `logout`, `searchImages`, `searchByImageFile`, `searchByImageUrl`.

| Endpoint imagem | Método | Uso |
|-----------------|--------|-----|
| `/search/images?q=` | GET | Texto → imagens (CSE `searchType=image`) |
| `/search/image` | POST multipart `file` | Foto → similares (Vision) |
| `/search/image` | POST JSON `{ "imageUrl": "..." }` | URL de imagem → similares |

Auth opcional (visitante pode usar). Warnings amigáveis: `PSE_UNAVAILABLE`, `VISION_UNAVAILABLE`, `IMAGE_BLOCKED`.

---

## 8. Estado e dados

- Sem Redux: `useState` + `useEffect` + `useSearchParams`  
- Suggest com debounce ~220ms  
- Sem cache de resultados (ACL sempre fresca)  

---

## 9. Visual / a11y (resumo)

- Tokens em `global.css` (Fraunces + Source Sans 3)  
- Label sr-only no campo; skeletons; mark âmbar  
- Responsivo &lt;640px: botão full-width  

Detalhe completo: **18.4**.

---

## 10. Como rodar

```powershell
cd apps\web
npm install
npm run dev
```

Abrir http://127.0.0.1:5173 — API precisa estar em `:8080` para busca interna.

---

## 11. Critérios de aceite da 18.7

- [ ] Rotas e funcionalidades F1–F12 presentes  
- [ ] Web com SafeSearch/filtro educacional  
- [ ] Admin com health + reindex + info PSE  
- [ ] Ajuda acessível  
- [ ] Build `npm run build` OK  

## 12. Critério de conclusão

Frontend documentado e coerente com `apps/web` → **18.7 concluída / pronta para envio**.
