# Política de busca web segura (escola)

Além do código ByBusca, configure no painel Google CSE
(https://programmablesearchengine.google.com/):

1. **SafeSearch** → ativo / filtrar conteúdo adulto  
2. **Sites to search (Include)** ←  
   [`SITES-INCLUIDOS.md`](./SITES-INCLUIDOS.md) ou  
   [`sites-incluidos-cse.txt`](./sites-incluidos-cse.txt)  
   Preferência: **Search only included sites**  
   (evita resultados de hotéis/viagem — ver [`DIAGNOSTICO-WEB-HOTEIS.md`](./DIAGNOSTICO-WEB-HOTEIS.md))  
3. **Sites to exclude** ←  
   [`SITES-EXCLUIDOS.md`](./SITES-EXCLUIDOS.md) ou  
   [`sites-excluidos-cse.txt`](./sites-excluidos-cse.txt)  
4. Inclua também o **domínio oficial da escola/rede**  
5. Confirme e salve (propagação pode levar minutos)

Engine atual:
- CX: `e54b7047fc7e64e10`
- URL: https://cse.google.com/cse?cx=e54b7047fc7e64e10

No código:
- Front: `apps/web/src/config/safeSearch.ts` (denylist + preferred)
- Back: `EducationalContentFilter` + `safe=active` na JSON API

---

## Busca por imagem (aba **Imagens**)

Dois fluxos distintos; políticas diferentes conforme a entrada.

| Entrada | API / serviço | Escopo web | SafeSearch |
|---------|---------------|------------|------------|
| **Texto** → imagens | Custom Search JSON `searchType=image` (`GET /api/v1/search/images?q=`) | **Allowlist** CSE — mesmos sites de [`SITES-INCLUIDOS.md`](./SITES-INCLUIDOS.md) / `sites-incluidos-cse.txt` | `safe=active` + pós-filtro `EducationalContentFilter` |
| **Foto** (upload ou URL) → similares | Google Cloud Vision Web Detection (`POST /api/v1/search/image`) | **Web mais ampla** (sem allowlist CSE); boost em hosts educacionais (`PREFERRED_HOST_FRAGMENTS`) | `SAFE_SEARCH_DETECTION` — rejeita adulto/violência; blocklist de hosts |

### Texto → imagens

- Mesmo CX do PSE (`GOOGLE_PSE_CX`); requer `GOOGLE_PSE_ENABLED=true` e `GOOGLE_PSE_API_KEY` no backend.
- Resultados só de domínios incluídos no engine CSE (modo “Search only included sites”).
- Domínios bloqueados em [`SITES-EXCLUIDOS.md`](./SITES-EXCLUIDOS.md) continuam excluídos no pós-filtro.

### Foto → páginas/imagens similares

- Upload **JPEG, PNG ou WebP**, máx. **5 MB** (`GOOGLE_VISION_MAX_UPLOAD_BYTES`), **ou** URL `http/https` no JSON `{ "imageUrl": "..." }`.
- Vision consulta a web aberta; **não** restringe à allowlist CSE.
- Ordenação favorece hosts educacionais; blocklist adulta/apostas/streaming igual à busca web texto.
- Upload **não** é persistido (processamento em memória).
- Feature flag: `GOOGLE_VISION_ENABLED` + `GOOGLE_VISION_API_KEY` (ver `.env.example`).

### Acesso

- Visitante na rede usa a aba **Imagens** **sem login** (endpoints públicos, como a busca web).

Spec: `docs/superpowers/specs/2026-08-04-bybusca-image-search-design.md`
