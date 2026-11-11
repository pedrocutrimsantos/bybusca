# ByBusca — Busca por imagem (texto + foto)

**Data:** 2026-08-04  
**Status:** **Implementado** — plano em `docs/superpowers/plans/2026-08-04-bybusca-image-search.md`  
**Produto:** ByBusca — Buscador Próprio (etapa 8)  
**Participante:** Pedro Cutrim  

## Problema

Alunos e professores precisam achar material visual educacional (diagramas, mapas, experimentos) e, às vezes, partir de uma **foto** (livro, lousa, figura) para páginas parecidas. Hoje o ByBusca cobre acervo escolar (FTS) + web educacional (CSE texto), mas **não** oferece busca de imagens nem busca reversa.

## Decisões

| Tema | Decisão |
|------|---------|
| Escopo | **C:** texto → imagens **e** busca por foto |
| Entrada foto | Upload **e** URL de imagem |
| UI | Nova aba/filtro **Imagens** (junto de Tudo / Web / Aulas…) |
| Política texto→imagens | Allowlist CSE (`sites-incluidos-cse.txt`) + SafeSearch |
| Política foto→similares | Web mais ampla + SafeSearch; boost hosts educacionais; blocklist adulta |
| Abordagem técnica | **1:** CSE/Custom Search imagens + Google Cloud Vision Web Detection |
| Quem usa | Visitante na rede (como web pública); ACL escolar não se aplica |
| Persistência upload | Não persistir após a busca (processar em memória / temp TTL curto) |

## Abordagens consideradas

| | Descrição | Resultado |
|---|-----------|-----------|
| **1 — CSE Imagens + Vision Web Detection (escolhida)** | Texto via CSE image; foto via Vision | Resultados no app; SafeSearch controlável |
| 2 — CSE + atalho Google Lens | Foto abre Lens externo | Rejeitada: sai do ByBusca |
| 3 — Só CSE | “Foto” vira labels → busca texto | Rejeitada: qualidade ruim |

## Arquitetura

```
[Aba Imagens]
    │
    ├─ Query texto ──► Google CSE / Custom Search (searchType=image)
    │                    CX ByBusca + allowlist + SafeSearch
    │                    ──► grade de imagens educacionais
    │
    └─ Upload JPG/PNG/WebP ou URL
           ──► API ByBusca (multipart ou JSON)
                 ──► Google Cloud Vision webDetection + SafeSearch
                 ──► lista páginas/imagens similares (+ boost educacional)
```

### Frontend (`apps/web`)

1. Filtro `IMAGE` em `Filters.tsx` (label **Imagens**).
2. `HomePage`: com `type=IMAGE`, esconder listagem interna (ou manter vazia) e mostrar painel de imagens.
3. Componente `ImageSearchPanel`:
   - busca por texto (reusa query `q` da URL);
   - zona upload + campo URL;
   - grade de resultados (thumbnail, título, link `_blank`).
4. Texto→imagens: preferir widget CSE com busca de imagem **ou** chamada à API backend que encapsula Custom Search `searchType=image` (mesmo CX). Decisão de implementação no plano: se o widget permitir `enableImageSearch` estável, usar; senão API JSON no backend.
5. Foto: `POST /api/v1/search/image` (multipart `file` **ou** JSON `{ "imageUrl": "..." }`).

### Backend (`apps/api`)

1. **`ImageSearchController`** + use cases:
   - `SearchImagesByTextUseCase` (opcional se front usar só CSE widget) — Custom Search JSON API `searchType=image`, `safe=active`, `cx` do PSE.
   - `SearchImagesByPhotoUseCase` — Vision `images:annotate` com `WEB_DETECTION` + `SAFE_SEARCH_DETECTION`; rejeitar se adult/violence altos.
2. Validação upload: `image/jpeg`, `image/png`, `image/webp`; máx. **5 MB**.
3. Validação URL: `http/https` apenas; timeout fetch curto; content-type imagem.
4. Env: `GOOGLE_VISION_ENABLED`, `GOOGLE_APPLICATION_CREDENTIALS` ou API key Vision conforme padrão do projeto; reutilizar `GOOGLE_PSE_*` para texto→imagem se via API.
5. Rate limit: mesmo filtro de busca (ou limite mais baixo para Vision).
6. Pós-filtro: `EducationalContentFilter` / blocklist de hosts; ordenar preferindo `PREFERRED_HOST_FRAGMENTS`.

### Segurança

- Secrets só no servidor.
- Não indexar nem gravar bytes da foto no Postgres no MVP.
- Mensagens de erro genéricas (sem stack / paths).
- CORS já restrito a localhost no scaffold.

## Fora do MVP

- OCR de lousa → query de texto  
- Imagens do acervo interno (FTS em arquivos)  
- Google Lens embutido  
- Persistência/histórico de fotos buscadas  

## Critérios de aceite

1. Aba **Imagens** visível nos filtros.  
2. Busca textual na aba Imagens retorna grade de imagens (allowlist + SafeSearch).  
3. Upload de JPG/PNG e URL válida retornam resultados similares na UI.  
4. Imagem com SafeSearch adulto/violento é rejeitada ou filtrada.  
5. Arquivo > 5 MB ou tipo inválido → 400 claro.  
6. Visitante consegue usar sem login.  
7. Documentação 18.3 / 18.7 / 18.9 atualizada com a feature.  

## Riscos

| Risco | Mitigação |
|-------|-----------|
| Quota/custo Vision | Feature flag `GOOGLE_VISION_ENABLED`; degradar com mensagem amigável |
| CSE image + allowlist = poucos resultados | Mensagem “refine o termo”; manter SafeSearch |
| Foto global sem allowlist | Blocklist + SafeSearch + boost educacional |
| Widget CSE image instável | Fallback Custom Search JSON no backend |

## Relação com docs existentes

- Política web: `docs/18.3-regras-fluxos-integracoes/POLITICA-BUSCA-WEB.md`  
- Allowlist: `sites-incluidos-cse.txt`  
- UX filtros: `docs/18.4-ux-telas-prototipos`, `docs/18.7-frontend-paineis`  
- Design geral: `docs/superpowers/specs/2026-07-24-bybusca-design.md`  
