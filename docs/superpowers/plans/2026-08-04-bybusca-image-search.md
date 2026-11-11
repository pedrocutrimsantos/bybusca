# ByBusca Image Search Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Status:** Implementado (Tasks 1–7 concluídas).

**Goal:** Entregar a aba **Imagens** no ByBusca com busca textual educacional (Custom Search `searchType=image` + allowlist/SafeSearch) e busca por foto (upload ou URL via Google Vision Web Detection + SafeSearch).

**Architecture:** Front ganha filtro `IMAGE` e `ImageSearchPanel`. Backend expõe `GET /api/v1/search/images?q=` (PSE image) e `POST /api/v1/search/image` (Vision). Mesmo padrão de `PseSearchService`/`RestClient`, feature flags, blocklist/boost de `EducationalContentFilter`. Uploads só em memória.

**Tech Stack:** Kotlin / Spring Boot 3.3, RestClient, Google Custom Search JSON API, Google Cloud Vision REST (`images:annotate`), React 18 / Vite / TypeScript.

**Spec:** `docs/superpowers/specs/2026-08-04-bybusca-image-search-design.md`

---

## File map

| Path | Responsibility |
|------|----------------|
| `apps/api/.../config/ByBuscaProperties.kt` | Props `vision.*` |
| `apps/api/.../application.yml` + `.env.example` | Env Vision |
| `apps/api/.../pse/PseSearchService.kt` | `searchImages(q, size)` |
| `apps/api/.../vision/VisionWebDetectionClient.kt` | Chamada Vision annotate |
| `apps/api/.../vision/ImageSearchService.kt` | Orquestra texto/foto + filtros |
| `apps/api/.../vision/ImageHit.kt` | DTO resultado imagem |
| `apps/api/.../vision/ImageUploadValidator.kt` | Validação MIME/tamanho |
| `apps/api/.../search/ImageSearchController.kt` | Endpoints GET/POST |
| `apps/api/.../config/SecurityConfig.kt` | Permit novos paths |
| `apps/api/src/test/.../ImageUploadValidatorTest.kt` | Unitários upload |
| `apps/api/src/test/.../ImageSearchServiceTest.kt` | Unitários SafeSearch |
| `apps/web/src/components/Filters.tsx` | Filtro Imagens |
| `apps/web/src/components/ImageSearchPanel.tsx` | UI texto + upload + URL + grade |
| `apps/web/src/api/client.ts` | Client image search |
| `apps/web/src/pages/HomePage.tsx` | Wire filtro IMAGE |
| CSS web existente | Grade de thumbs |
| Docs 18.3 / 18.7 / 18.9 / 18.4 | Documentar feature |

**Decisão travada:** texto→imagens via **API Custom Search** (`searchType=image`) para grade unificada com foto — não widget CSE image.

---

### Task 1: Props Vision + DTO + teste de validação

**Files:**
- Modify: `apps/api/src/main/kotlin/br/bybusca/config/ByBuscaProperties.kt`
- Modify: `apps/api/src/main/resources/application.yml`
- Modify: `.env.example`
- Create: `apps/api/src/main/kotlin/br/bybusca/vision/ImageHit.kt`
- Create: `apps/api/src/main/kotlin/br/bybusca/vision/ImageUploadValidator.kt`
- Create: `apps/api/src/test/kotlin/br/bybusca/vision/ImageUploadValidatorTest.kt`

- [ ] **Step 1: Write failing tests for upload validator**

```kotlin
package br.bybusca.vision

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile

class ImageUploadValidatorTest {
    private val v = ImageUploadValidator(maxBytes = 5 * 1024 * 1024)

    @Test
    fun `rejects empty file`() {
        val f = MockMultipartFile("file", "x.jpg", "image/jpeg", ByteArray(0))
        assertThrows(IllegalArgumentException::class.java) { v.requireValid(f) }
    }

    @Test
    fun `rejects non-image content type`() {
        val f = MockMultipartFile("file", "x.pdf", "application/pdf", byteArrayOf(1, 2, 3))
        assertThrows(IllegalArgumentException::class.java) { v.requireValid(f) }
    }

    @Test
    fun `accepts jpeg under limit`() {
        val f = MockMultipartFile("file", "x.jpg", "image/jpeg", ByteArray(100) { 1 })
        assertEquals("image/jpeg", v.requireValid(f))
    }
}
```

- [ ] **Step 2: Run test — expect FAIL (class missing)**

```powershell
cd C:\Users\User\Repository\ByBusca\apps\api ; .\gradlew.bat test --tests br.bybusca.vision.ImageUploadValidatorTest --no-daemon
```

- [ ] **Step 3: Implement validator + DTO + props**

`ImageHit.kt`:

```kotlin
package br.bybusca.vision

data class ImageHit(
    val title: String,
    val pageUrl: String,
    val imageUrl: String,
    val thumbnailUrl: String?,
    val score: Double,
    val source: String // "pse-image" | "vision"
)
```

`ImageUploadValidator.kt`:

```kotlin
package br.bybusca.vision

import org.springframework.web.multipart.MultipartFile

class ImageUploadValidator(private val maxBytes: Long) {
    private val allowed = setOf("image/jpeg", "image/png", "image/webp")

    fun requireValid(file: MultipartFile): String {
        if (file.isEmpty) throw IllegalArgumentException("Arquivo de imagem obrigatorio")
        if (file.size > maxBytes) throw IllegalArgumentException("Imagem excede 5 MB")
        val ct = (file.contentType ?: "").lowercase()
        if (ct !in allowed) throw IllegalArgumentException("Tipo invalido. Use JPEG, PNG ou WebP")
        return ct
    }
}
```

Extend `ByBuscaProperties` with:

```kotlin
var vision: VisionProps = VisionProps()

data class VisionProps(
    var enabled: Boolean = false,
    var apiKey: String = "",
    var timeoutMs: Long = 4000,
    var maxUploadBytes: Long = 5L * 1024 * 1024
)
```

`application.yml` under `bybusca:`:

```yaml
  vision:
    enabled: ${GOOGLE_VISION_ENABLED:false}
    api-key: ${GOOGLE_VISION_API_KEY:}
    timeout-ms: ${GOOGLE_VISION_TIMEOUT_MS:4000}
    max-upload-bytes: ${GOOGLE_VISION_MAX_UPLOAD_BYTES:5242880}
```

`.env.example` add:

```env
GOOGLE_VISION_ENABLED=false
GOOGLE_VISION_API_KEY=
GOOGLE_VISION_TIMEOUT_MS=4000
```

- [ ] **Step 4: Run tests — expect PASS**

```powershell
.\gradlew.bat test --tests br.bybusca.vision.ImageUploadValidatorTest --no-daemon
```

- [ ] **Step 5: Commit**

```powershell
git add apps/api/src/main/kotlin/br/bybusca/config/ByBuscaProperties.kt apps/api/src/main/resources/application.yml apps/api/src/main/kotlin/br/bybusca/vision .env.example apps/api/src/test/kotlin/br/bybusca/vision
git commit --trailer "Co-authored-by: Cursor <cursoragent@cursor.com>" -m "feat(api): add vision props and image upload validator"
```

---

### Task 2: PSE searchType=image

**Files:**
- Modify: `apps/api/src/main/kotlin/br/bybusca/pse/PseSearchService.kt`

- [ ] **Step 1: Add `searchImages` to `PseSearchService`**

```kotlin
fun searchImages(query: String, size: Int): Pair<List<br.bybusca.vision.ImageHit>, String?> {
    if (!props.pse.enabled || props.pse.apiKey.isBlank() || props.pse.cx.isBlank()) {
        return emptyList<br.bybusca.vision.ImageHit>() to null
    }
    if (failures.get() >= 3) {
        return emptyList<br.bybusca.vision.ImageHit>() to "PSE_UNAVAILABLE"
    }
    return try {
        val q = query.trim()
        if (q.isBlank()) return emptyList<br.bybusca.vision.ImageHit>() to null
        val encoded = java.net.URLEncoder.encode(q, Charsets.UTF_8)
        val path =
            "/customsearch/v1?key=${props.pse.apiKey}&cx=${props.pse.cx}" +
                "&q=$encoded&searchType=image&num=${size.coerceIn(1, 10)}&safe=active"
        @Suppress("UNCHECKED_CAST")
        val body = client.get().uri(path).retrieve().body(Map::class.java) as Map<String, Any?>?
        val items = (body?.get("items") as? List<Map<String, Any?>>).orEmpty().mapNotNull { item ->
            val imgMeta = item["image"] as? Map<*, *>
            val pageUrl = imgMeta?.get("contextLink")?.toString()
                ?: item["link"]?.toString().orEmpty()
            val imageUrl = item["link"]?.toString().orEmpty()
            if (imageUrl.isBlank()) return@mapNotNull null
            if (EducationalContentFilter.isBlocked(pageUrl) || EducationalContentFilter.isBlocked(imageUrl)) {
                return@mapNotNull null
            }
            val thumb = imgMeta?.get("thumbnailLink")?.toString()
            br.bybusca.vision.ImageHit(
                title = item["title"]?.toString().orEmpty(),
                pageUrl = pageUrl.ifBlank { imageUrl },
                imageUrl = imageUrl,
                thumbnailUrl = thumb,
                score = 0.5 + EducationalContentFilter.scoreBoost(pageUrl),
                source = "pse-image"
            )
        }.sortedByDescending { it.score }.take(size.coerceIn(1, 10))
        failures.set(0)
        items to null
    } catch (ex: Exception) {
        failures.incrementAndGet()
        log.warn("PSE image indisponivel: {}", ex.message)
        emptyList<br.bybusca.vision.ImageHit>() to "PSE_UNAVAILABLE"
    }
}
```

- [ ] **Step 2: Compile**

```powershell
.\gradlew.bat compileKotlin --no-daemon
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```powershell
git add apps/api/src/main/kotlin/br/bybusca/pse/PseSearchService.kt
git commit --trailer "Co-authored-by: Cursor <cursoragent@cursor.com>" -m "feat(api): Custom Search image mode for educational images"
```

---

### Task 3: Vision Web Detection + ImageSearchService

**Files:**
- Create: `apps/api/src/main/kotlin/br/bybusca/vision/VisionWebDetectionClient.kt`
- Create: `apps/api/src/main/kotlin/br/bybusca/vision/ImageSearchService.kt`
- Create: `apps/api/src/test/kotlin/br/bybusca/vision/ImageSearchServiceTest.kt`

- [ ] **Step 1: Failing SafeSearch tests**

```kotlin
package br.bybusca.vision

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ImageSearchServiceTest {
    @Test
    fun `safeSearch blocks adult`() {
        assertTrue(
            ImageSearchService.isUnsafeSafeSearch(
                mapOf("adult" to "LIKELY", "violence" to "VERY_UNLIKELY")
            )
        )
    }

    @Test
    fun `safeSearch allows clean`() {
        assertFalse(
            ImageSearchService.isUnsafeSafeSearch(
                mapOf("adult" to "VERY_UNLIKELY", "violence" to "UNLIKELY", "racy" to "UNLIKELY")
            )
        )
    }
}
```

- [ ] **Step 2: Run — expect FAIL**

```powershell
.\gradlew.bat test --tests br.bybusca.vision.ImageSearchServiceTest --no-daemon
```

- [ ] **Step 3: Implement Vision client + ImageSearchService**

`VisionWebDetectionClient`: POST `https://vision.googleapis.com/v1/images:annotate?key=API_KEY` with features `WEB_DETECTION` + `SAFE_SEARCH_DETECTION`. Image via `content` (base64) or `source.imageUri`.

If SafeSearch unsafe → return `emptyList() to "IMAGE_BLOCKED"`.

Map `pagesWithMatchingImages` / `visuallySimilarImages` to `ImageHit` (`source = "vision"`), then filter blocklist and boost preferred hosts.

`ImageSearchService`:

```kotlin
@Service
class ImageSearchService(
    private val props: ByBuscaProperties,
    private val pse: PseSearchService,
    private val vision: VisionWebDetectionClient
) {
    private val uploadValidator = ImageUploadValidator(props.vision.maxUploadBytes)

    companion object {
        private val bad = setOf("LIKELY", "VERY_LIKELY")
        fun isUnsafeSafeSearch(ann: Map<String, String>): Boolean {
            val adult = ann["adult"]?.uppercase() ?: "UNKNOWN"
            val violence = ann["violence"]?.uppercase() ?: "UNKNOWN"
            val racy = ann["racy"]?.uppercase() ?: "UNKNOWN"
            return adult in bad || violence in bad || racy == "VERY_LIKELY"
        }
    }

    fun byText(q: String, size: Int = 10) = pse.searchImages(q, size)

    fun byUpload(file: org.springframework.web.multipart.MultipartFile): Pair<List<ImageHit>, String?> {
        if (!props.vision.enabled || props.vision.apiKey.isBlank()) {
            return emptyList<ImageHit>() to "VISION_UNAVAILABLE"
        }
        uploadValidator.requireValid(file)
        return vision.detect(contentBase64 = java.util.Base64.getEncoder().encodeToString(file.bytes), imageUri = null)
    }

    fun byUrl(imageUrl: String): Pair<List<ImageHit>, String?> {
        if (!props.vision.enabled || props.vision.apiKey.isBlank()) {
            return emptyList<ImageHit>() to "VISION_UNAVAILABLE"
        }
        val u = imageUrl.trim()
        require(u.startsWith("http://") || u.startsWith("https://")) { "URL invalida" }
        return vision.detect(contentBase64 = null, imageUri = u)
    }
}
```

- [ ] **Step 4: Tests PASS**

```powershell
.\gradlew.bat test --tests br.bybusca.vision.* --no-daemon
```

- [ ] **Step 5: Commit**

```powershell
git add apps/api/src/main/kotlin/br/bybusca/vision apps/api/src/test/kotlin/br/bybusca/vision
git commit --trailer "Co-authored-by: Cursor <cursoragent@cursor.com>" -m "feat(api): Vision web detection for reverse image search"
```

---

### Task 4: HTTP controller + security

**Files:**
- Create: `apps/api/src/main/kotlin/br/bybusca/search/ImageSearchController.kt`
- Modify: `apps/api/src/main/kotlin/br/bybusca/config/SecurityConfig.kt`

- [ ] **Step 1: Controller**

```kotlin
@RestController
@RequestMapping("/api/v1")
class ImageSearchController(private val images: ImageSearchService) {
    data class ImageSearchResponse(val items: List<ImageHit>, val warnings: List<String> = emptyList())
    data class ImageUrlRequest(val imageUrl: String)

    @GetMapping("/search/images")
    fun byText(@RequestParam q: String, @RequestParam(defaultValue = "10") size: Int): ImageSearchResponse {
        val (items, warn) = images.byText(q, size)
        return ImageSearchResponse(items, listOfNotNull(warn))
    }

    @PostMapping("/search/image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun byFile(@RequestParam("file") file: MultipartFile): ResponseEntity<Any> = try {
        val (items, warn) = images.byUpload(file)
        ResponseEntity.ok(ImageSearchResponse(items, listOfNotNull(warn)))
    } catch (ex: IllegalArgumentException) {
        ResponseEntity.badRequest().body(mapOf("code" to "BAD_REQUEST", "message" to (ex.message ?: "Dados invalidos")))
    }

    @PostMapping("/search/image", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun byUrl(@RequestBody body: ImageUrlRequest): ResponseEntity<Any> = try {
        val (items, warn) = images.byUrl(body.imageUrl)
        ResponseEntity.ok(ImageSearchResponse(items, listOfNotNull(warn)))
    } catch (ex: IllegalArgumentException) {
        ResponseEntity.badRequest().body(mapOf("code" to "BAD_REQUEST", "message" to (ex.message ?: "Dados invalidos")))
    }
}
```

- [ ] **Step 2: SecurityConfig** — add `/api/v1/search/images` and `/api/v1/search/image` to permitAll list (redundant with `/api/v1/**` but explicit).

- [ ] **Step 3: Smoke**

```powershell
Invoke-RestMethod "http://127.0.0.1:8081/api/v1/search/images?q=wikipedia"
```

Expected: JSON with `items` array (possibly empty if PSE disabled).

- [ ] **Step 4: Commit**

```powershell
git add apps/api/src/main/kotlin/br/bybusca/search/ImageSearchController.kt apps/api/src/main/kotlin/br/bybusca/config/SecurityConfig.kt
git commit --trailer "Co-authored-by: Cursor <cursoragent@cursor.com>" -m "feat(api): expose image search HTTP endpoints"
```

---

### Task 5: Frontend filter + API client

**Files:**
- Modify: `apps/web/src/components/Filters.tsx`
- Modify: `apps/web/src/api/client.ts`

- [ ] **Step 1:** Add `{ id: 'IMAGE', label: 'Imagens' }` after WEB in `FILTERS`.

- [ ] **Step 2:** Add types + `searchImages` / `searchByImageFile` / `searchByImageUrl` in `client.ts` calling `/search/images` and `/search/image` (multipart vs JSON). Reuse existing auth header helpers.

- [ ] **Step 3: Commit**

```powershell
git add apps/web/src/components/Filters.tsx apps/web/src/api/client.ts
git commit --trailer "Co-authored-by: Cursor <cursoragent@cursor.com>" -m "feat(web): IMAGE filter and image search API client"
```

---

### Task 6: ImageSearchPanel + HomePage

**Files:**
- Create: `apps/web/src/components/ImageSearchPanel.tsx`
- Modify: `apps/web/src/pages/HomePage.tsx`
- Modify: CSS existente do web

- [ ] **Step 1:** `ImageSearchPanel({ query })`:
  - effect: se `query` → `searchImages(query)`
  - upload + URL form → `searchByImageFile` / `searchByImageUrl`
  - grade de thumbs com link `pageUrl` `_blank`
  - warnings amigáveis (`PSE_UNAVAILABLE`, `VISION_UNAVAILABLE`, `IMAGE_BLOCKED`)

- [ ] **Step 2:** HomePage wiring:

```ts
const showImages = typeParam === 'IMAGE'
const showInternal = typeParam !== 'WEB' && typeParam !== 'IMAGE'
const showCse = typeParam === 'WEB' || typeParam === 'ALL'
```

Render `<ImageSearchPanel query={qParam} />` when `showImages`.

- [ ] **Step 3: Manual check** — `/?q=wikipedia&type=IMAGE`

- [ ] **Step 4: Commit**

```powershell
git add apps/web/src/components/ImageSearchPanel.tsx apps/web/src/pages/HomePage.tsx
git commit --trailer "Co-authored-by: Cursor <cursoragent@cursor.com>" -m "feat(web): ImageSearchPanel on Imagens filter"
```

---

### Task 7: Docs

**Files:**
- Modify: `docs/18.3-regras-fluxos-integracoes/POLITICA-BUSCA-WEB.md`
- Modify: `docs/18.7-frontend-paineis/README.md`
- Modify: `docs/18.9-manual-uso-operacao-suporte/README.md`
- Modify: `docs/18.4-ux-telas-prototipos/README.md`
- Modify: `docs/superpowers/specs/2026-08-04-bybusca-image-search-design.md` (Status → Implementado ao concluir)

- [x] **Step 1:** Documentar política texto=allowlist vs foto=web ampla + SafeSearch.

- [x] **Step 2:** Manual: aba Imagens, upload, URL, limite 5 MB, flags env.

- [ ] **Step 3: Commit**

```powershell
git add docs
git commit --trailer "Co-authored-by: Cursor <cursoragent@cursor.com>" -m "docs: document image search policy and usage"
```

---

## Spec coverage

| Spec | Task |
|------|------|
| Aba Imagens | 5–6 |
| Texto allowlist + SafeSearch | 2, 4, 6 |
| Upload + URL | 1, 3, 4, 6 |
| SafeSearch Vision | 3 |
| 5 MB / MIME | 1, 4 |
| Visitante | 4 |
| Docs | 7 |
| Flag Vision | 1, 3 |
| Sem persistir upload | 3 |

## Self-review

- Sem TBD de implementação.
- Nomes estáveis: `ImageHit`, `ImageSearchService`, `/search/images`, `/search/image`.
