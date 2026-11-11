package br.bybusca.pse

import br.bybusca.config.ByBuscaProperties
import br.bybusca.search.SearchHit
import br.bybusca.vision.ImageHit
import org.slf4j.LoggerFactory
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.util.concurrent.atomic.AtomicInteger

@Service
class PseSearchService(
    private val props: ByBuscaProperties
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val failures = AtomicInteger(0)

    private val client: RestClient by lazy {
        val factory = SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(props.pse.timeoutMs.toInt())
            setReadTimeout(props.pse.timeoutMs.toInt())
        }
        RestClient.builder()
            .baseUrl("https://www.googleapis.com")
            .requestFactory(factory)
            .build()
    }

    fun search(query: String, size: Int): Pair<List<SearchHit>, String?> {
        if (!props.pse.enabled || props.pse.apiKey.isBlank() || props.pse.cx.isBlank()) {
            return emptyList<SearchHit>() to null
        }
        if (failures.get() >= 3) {
            return emptyList<SearchHit>() to "PSE_UNAVAILABLE"
        }
        return try {
            val safeQuery = EducationalContentFilter.buildSafeWebQuery(query)
            val encoded = java.net.URLEncoder.encode(safeQuery, Charsets.UTF_8)
            // safe=active = SafeSearch Google (conteúdo adulto)
            val path =
                "/customsearch/v1?key=${props.pse.apiKey}&cx=${props.pse.cx}" +
                    "&q=$encoded&num=${(size + 5).coerceIn(1, 10)}&safe=active"
            @Suppress("UNCHECKED_CAST")
            val body = client.get()
                .uri(path)
                .retrieve()
                .body(Map::class.java) as Map<String, Any?>?
            val items = (body?.get("items") as? List<Map<String, Any?>>).orEmpty()
                .mapNotNull { item ->
                    val url = item["link"]?.toString().orEmpty()
                    if (url.isBlank() || EducationalContentFilter.isBlocked(url)) return@mapNotNull null
                    SearchHit(
                        id = url,
                        type = "WEB",
                        title = item["title"]?.toString() ?: "",
                        snippet = item["snippet"]?.toString() ?: "",
                        url = url,
                        score = 0.45 + EducationalContentFilter.scoreBoost(url),
                        source = "pse"
                    )
                }
                .sortedByDescending { it.score }
                .take(size.coerceIn(1, 10))
            failures.set(0)
            items to null
        } catch (ex: Exception) {
            failures.incrementAndGet()
            log.warn("PSE indisponível: {}", ex.message)
            emptyList<SearchHit>() to "PSE_UNAVAILABLE"
        }
    }

    fun searchImages(query: String, size: Int): Pair<List<ImageHit>, String?> {
        if (!props.pse.enabled || props.pse.apiKey.isBlank() || props.pse.cx.isBlank()) {
            return emptyList<ImageHit>() to "PSE_UNAVAILABLE"
        }
        if (failures.get() >= 3) {
            return emptyList<ImageHit>() to "PSE_UNAVAILABLE"
        }
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            return emptyList<ImageHit>() to null
        }
        return try {
            val encoded = java.net.URLEncoder.encode(trimmed, Charsets.UTF_8)
            val num = size.coerceIn(1, 10)
            val path =
                "/customsearch/v1?key=${props.pse.apiKey}&cx=${props.pse.cx}" +
                    "&q=$encoded&searchType=image&num=$num&safe=active"
            @Suppress("UNCHECKED_CAST")
            val body = client.get()
                .uri(path)
                .retrieve()
                .body(Map::class.java) as Map<String, Any?>?
            val items = (body?.get("items") as? List<Map<String, Any?>>).orEmpty()
                .mapNotNull { item ->
                    val imageUrl = item["link"]?.toString().orEmpty()
                    if (imageUrl.isBlank()) return@mapNotNull null
                    val imageMeta = item["image"] as? Map<String, Any?>
                    val pageUrl = imageMeta?.get("contextLink")?.toString()?.takeIf { it.isNotBlank() }
                        ?: imageUrl
                    if (EducationalContentFilter.isBlocked(pageUrl) ||
                        EducationalContentFilter.isBlocked(imageUrl)
                    ) {
                        return@mapNotNull null
                    }
                    ImageHit(
                        title = item["title"]?.toString() ?: "",
                        pageUrl = pageUrl,
                        imageUrl = imageUrl,
                        thumbnailUrl = imageMeta?.get("thumbnailLink")?.toString(),
                        score = 0.5 + EducationalContentFilter.scoreBoost(pageUrl),
                        source = "pse-image"
                    )
                }
            failures.set(0)
            items to null
        } catch (ex: Exception) {
            failures.incrementAndGet()
            log.warn("PSE indisponível (imagens): {}", ex.message)
            emptyList<ImageHit>() to "PSE_UNAVAILABLE"
        }
    }
}
